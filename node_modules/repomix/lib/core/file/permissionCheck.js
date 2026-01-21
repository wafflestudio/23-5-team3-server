var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { constants } from 'node:fs';
import * as fs from 'node:fs/promises';
import { platform } from 'node:os';
import { logger } from '../../shared/logger.js';
export class PermissionError extends Error {
    constructor(message, path, code) {
        super(message);
        this.path = path;
        this.code = code;
        this.name = 'PermissionError';
    }
}
export const checkDirectoryPermissions = (dirPath) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        // First try to read directory contents
        yield fs.readdir(dirPath);
        // Check specific permissions
        const details = {
            read: false,
            write: false,
            execute: false,
        };
        try {
            yield fs.access(dirPath, constants.R_OK);
            details.read = true;
        }
        catch (_a) { }
        try {
            yield fs.access(dirPath, constants.W_OK);
            details.write = true;
        }
        catch (_b) { }
        try {
            yield fs.access(dirPath, constants.X_OK);
            details.execute = true;
        }
        catch (_c) { }
        const hasAllPermissions = details.read && details.write && details.execute;
        if (!hasAllPermissions) {
            return {
                hasAllPermission: false,
                details,
            };
        }
        return {
            hasAllPermission: true,
            details,
        };
    }
    catch (error) {
        if (error instanceof Error && 'code' in error) {
            switch (error.code) {
                case 'EPERM':
                case 'EACCES':
                case 'EISDIR':
                    return {
                        hasAllPermission: false,
                        error: new PermissionError(getMacOSPermissionMessage(dirPath, error.code), dirPath, error.code),
                    };
                default:
                    logger.debug('Directory permission check error:', error);
                    return {
                        hasAllPermission: false,
                        error: error,
                    };
            }
        }
        return {
            hasAllPermission: false,
            error: error instanceof Error ? error : new Error(String(error)),
        };
    }
});
const getMacOSPermissionMessage = (dirPath, errorCode) => {
    if (platform() === 'darwin') {
        return `Permission denied: Cannot access '${dirPath}', error code: ${errorCode}.

This error often occurs when macOS security restrictions prevent access to the directory.
To fix this:

1. Open System Settings
2. Navigate to Privacy & Security > Files and Folders
3. Find your terminal app (Terminal.app, iTerm2, VS Code, etc.)
4. Grant necessary folder access permissions

If your terminal app is not listed:
- Try running repomix command again
- When prompted by macOS, click "Allow"
- Restart your terminal app if needed
`;
    }
    return `Permission denied: Cannot access '${dirPath}'`;
};
//# sourceMappingURL=permissionCheck.js.map