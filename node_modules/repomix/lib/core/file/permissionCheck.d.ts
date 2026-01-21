export interface PermissionCheckResult {
    hasAllPermission: boolean;
    error?: Error;
    details?: {
        read?: boolean;
        write?: boolean;
        execute?: boolean;
    };
}
export declare class PermissionError extends Error {
    readonly path: string;
    readonly code?: string | undefined;
    constructor(message: string, path: string, code?: string | undefined);
}
export declare const checkDirectoryPermissions: (dirPath: string) => Promise<PermissionCheckResult>;
//# sourceMappingURL=permissionCheck.d.ts.map