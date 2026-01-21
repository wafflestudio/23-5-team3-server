var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import * as fs from 'node:fs/promises';
import path from 'node:path';
import * as prompts from '@clack/prompts';
import pc from 'picocolors';
import { getGlobalDirectory } from '../../config/globalDirectory.js';
import { logger } from '../../shared/logger.js';
/**
 * Check if a file exists at the given path
 */
const fileExists = (filePath) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        yield fs.access(filePath);
        return true;
    }
    catch (_a) {
        return false;
    }
});
/**
 * Replace all occurrences of 'repopack' with 'repomix' in a string
 */
const replaceRepopackString = (content) => {
    return content.replace(/repopack/g, 'repomix').replace(/Repopack/g, 'Repomix');
};
/**
 * Update file content by replacing 'repopack' with 'repomix'
 */
const updateFileContent = (filePath) => __awaiter(void 0, void 0, void 0, function* () {
    const content = yield fs.readFile(filePath, 'utf8');
    const updatedContent = replaceRepopackString(content);
    // Check if content needs to be updated
    if (content !== updatedContent) {
        yield fs.writeFile(filePath, updatedContent, 'utf8');
        const relativePath = path.relative(process.cwd(), filePath);
        logger.log(`Updated repopack references in ${pc.cyan(relativePath)}`);
        return true;
    }
    return false;
});
/**
 * Parse JSON content, update instructionFilePath if exists
 */
const updateInstructionPath = (content) => {
    var _a, _b;
    try {
        const config = JSON.parse(content);
        if ((_a = config.output) === null || _a === void 0 ? void 0 : _a.instructionFilePath) {
            config.output.instructionFilePath = config.output.instructionFilePath.replace('repopack', 'repomix');
        }
        // Also update output.filePath if it exists
        if ((_b = config.output) === null || _b === void 0 ? void 0 : _b.filePath) {
            config.output.filePath = config.output.filePath.replace('repopack', 'repomix');
        }
        return JSON.stringify(config, null, 2);
    }
    catch (_c) {
        return content;
    }
};
/**
 * Get output file paths pairs
 */
const getOutputFilePaths = (rootDir) => {
    const extensions = ['.txt', '.xml', '.md'];
    const oldPaths = extensions.map((ext) => path.join(rootDir, `repopack-output${ext}`));
    const newPaths = extensions.map((ext) => path.join(rootDir, `repomix-output${ext}`));
    return { oldPaths, newPaths };
};
/**
 * Migrate a single file from old path to new path
 */
const migrateFile = (oldPath_1, newPath_1, description_1, ...args_1) => __awaiter(void 0, [oldPath_1, newPath_1, description_1, ...args_1], void 0, function* (oldPath, newPath, description, isConfig = false) {
    if (!(yield fileExists(oldPath))) {
        return false;
    }
    const exists = yield fileExists(newPath);
    if (exists) {
        const shouldOverwrite = yield prompts.confirm({
            message: `${description} already exists at ${newPath}. Do you want to overwrite it?`,
        });
        if (prompts.isCancel(shouldOverwrite) || !shouldOverwrite) {
            logger.info(`Skipping migration of ${description}`);
            return false;
        }
    }
    try {
        // Read and update content
        let content = yield fs.readFile(oldPath, 'utf8');
        content = replaceRepopackString(content);
        // For config files, also update instructionFilePath and output.filePath
        if (isConfig) {
            content = updateInstructionPath(content);
        }
        // Ensure the target directory exists
        yield fs.mkdir(path.dirname(newPath), { recursive: true });
        // Write to new file
        yield fs.writeFile(newPath, content, 'utf8');
        // Remove old file
        yield fs.unlink(oldPath);
        const relativeOldPath = path.relative(process.cwd(), oldPath);
        const relativeNewPath = path.relative(process.cwd(), newPath);
        logger.log(`Renamed ${description} from ${relativeOldPath} to ${relativeNewPath}`);
        return true;
    }
    catch (error) {
        logger.error(`Failed to migrate ${description}:`, error);
        return false;
    }
});
/**
 * Update content of gitignore and repomixignore files
 */
const updateIgnoreFiles = (rootDir) => __awaiter(void 0, void 0, void 0, function* () {
    const gitignorePath = path.join(rootDir, '.gitignore');
    const repomixignorePath = path.join(rootDir, '.repomixignore');
    if (yield fileExists(gitignorePath)) {
        const updated = yield updateFileContent(gitignorePath);
        if (!updated) {
            logger.debug('No changes needed in .gitignore');
        }
    }
    if (yield fileExists(repomixignorePath)) {
        const updated = yield updateFileContent(repomixignorePath);
        if (!updated) {
            logger.debug('No changes needed in .repomixignore');
        }
    }
});
/**
 * Get all migration related file paths
 */
const getMigrationPaths = (rootDir) => {
    const { oldPaths: oldOutputPaths, newPaths: newOutputPaths } = getOutputFilePaths(rootDir);
    const oldGlobalDirectory = path.join(process.env.HOME || '', '.config', 'repopack');
    const newGlobalDirectory = getGlobalDirectory();
    return {
        oldConfigPath: path.join(rootDir, 'repopack.config.json'),
        newConfigPath: path.join(rootDir, 'repomix.config.json'),
        oldIgnorePath: path.join(rootDir, '.repopackignore'),
        newIgnorePath: path.join(rootDir, '.repomixignore'),
        oldInstructionPath: path.join(rootDir, 'repopack-instruction.md'),
        newInstructionPath: path.join(rootDir, 'repomix-instruction.md'),
        oldOutputPaths,
        newOutputPaths,
        oldGlobalConfigPath: path.join(oldGlobalDirectory, 'repopack.config.json'),
        newGlobalConfigPath: path.join(newGlobalDirectory, 'repomix.config.json'),
    };
};
/**
 * Migrate output files
 */
const migrateOutputFiles = (oldPaths, newPaths) => __awaiter(void 0, void 0, void 0, function* () {
    const migratedFiles = [];
    for (let i = 0; i < oldPaths.length; i++) {
        const oldPath = oldPaths[i];
        const newPath = newPaths[i];
        const ext = path.extname(oldPath);
        if (yield migrateFile(oldPath, newPath, `Output file (${ext})`)) {
            migratedFiles.push(newPath);
        }
    }
    return migratedFiles;
});
export const runMigrationAction = (rootDir) => __awaiter(void 0, void 0, void 0, function* () {
    const result = {
        configMigrated: false,
        ignoreMigrated: false,
        instructionMigrated: false,
        outputFilesMigrated: [],
        globalConfigMigrated: false,
    };
    try {
        const paths = getMigrationPaths(rootDir);
        // Check if migration is needed
        const hasOldConfig = yield fileExists(paths.oldConfigPath);
        const hasOldIgnore = yield fileExists(paths.oldIgnorePath);
        const hasOldInstruction = yield fileExists(paths.oldInstructionPath);
        const hasOldGlobalConfig = yield fileExists(paths.oldGlobalConfigPath);
        const hasOldOutput = yield Promise.all(paths.oldOutputPaths.map(fileExists)).then((results) => results.some((exists) => exists));
        if (!hasOldConfig && !hasOldIgnore && !hasOldInstruction && !hasOldOutput && !hasOldGlobalConfig) {
            logger.debug('No Repopack files found to migrate.');
            return result;
        }
        // Show migration notice based on what needs to be migrated
        let migrationMessage = `Found ${pc.green('Repopack')} `;
        const items = [];
        if (hasOldConfig || hasOldIgnore || hasOldInstruction || hasOldOutput)
            items.push('local configuration');
        if (hasOldGlobalConfig)
            items.push('global configuration');
        migrationMessage += `${items.join(' and ')}. Would you like to migrate to ${pc.green('Repomix')}?`;
        // Confirm migration with user
        const shouldMigrate = yield prompts.confirm({
            message: migrationMessage,
        });
        if (prompts.isCancel(shouldMigrate) || !shouldMigrate) {
            logger.info('Migration cancelled.');
            return result;
        }
        // Show migration notice
        logger.info(pc.cyan('\nMigrating from Repopack to Repomix...'));
        logger.log('');
        // Migrate config file
        if (hasOldConfig) {
            result.configMigrated = yield migrateFile(paths.oldConfigPath, paths.newConfigPath, 'Configuration file', true);
        }
        // Migrate global config file
        if (hasOldGlobalConfig) {
            result.globalConfigMigrated = yield migrateFile(paths.oldGlobalConfigPath, paths.newGlobalConfigPath, 'Global configuration file', true);
        }
        // Migrate ignore file
        if (hasOldIgnore) {
            result.ignoreMigrated = yield migrateFile(paths.oldIgnorePath, paths.newIgnorePath, 'Ignore file');
        }
        // Migrate instruction file
        if (hasOldInstruction) {
            result.instructionMigrated = yield migrateFile(paths.oldInstructionPath, paths.newInstructionPath, 'Instruction file');
        }
        // Migrate output files
        if (hasOldOutput) {
            result.outputFilesMigrated = yield migrateOutputFiles(paths.oldOutputPaths, paths.newOutputPaths);
        }
        // Update content in gitignore and repomixignore
        yield updateIgnoreFiles(rootDir);
        // Show success message
        if (result.configMigrated ||
            result.ignoreMigrated ||
            result.instructionMigrated ||
            result.outputFilesMigrated.length > 0 ||
            result.globalConfigMigrated) {
            logger.log('');
            logger.success('✔ Migration completed successfully!');
            logger.log('');
            logger.info('You can now use Repomix commands as usual. The old Repopack files have been migrated to the new format.');
            logger.log('');
        }
        return result;
    }
    catch (error) {
        if (error instanceof Error) {
            result.error = error;
        }
        else {
            result.error = new Error(String(error));
        }
        logger.error('An error occurred during migration:', error);
        return result;
    }
});
//# sourceMappingURL=migrationAction.js.map