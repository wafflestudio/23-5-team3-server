interface MigrationResult {
    configMigrated: boolean;
    ignoreMigrated: boolean;
    instructionMigrated: boolean;
    outputFilesMigrated: string[];
    globalConfigMigrated: boolean;
    error?: Error;
}
export declare const runMigrationAction: (rootDir: string) => Promise<MigrationResult>;
export {};
//# sourceMappingURL=migrationAction.d.ts.map