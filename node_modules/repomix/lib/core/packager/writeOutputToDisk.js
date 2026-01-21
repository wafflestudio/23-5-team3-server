var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import fs from 'node:fs/promises';
import path from 'node:path';
import { logger } from '../../shared/logger.js';
// Write output to file or stdout
export const writeOutputToDisk = (output, config) => __awaiter(void 0, void 0, void 0, function* () {
    // Write to stdout
    if (config.output.stdout === true) {
        process.stdout.write(output);
        return;
    }
    // Normal case: write to file
    const outputPath = path.resolve(config.cwd, config.output.filePath);
    logger.trace(`Writing output to: ${outputPath}`);
    // Create output directory if it doesn't exist
    yield fs.mkdir(path.dirname(outputPath), { recursive: true });
    yield fs.writeFile(outputPath, output);
});
//# sourceMappingURL=writeOutputToDisk.js.map