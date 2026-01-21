/**
 * Splits comma-separated glob patterns while preserving brace expansion patterns.
 * This ensures patterns with braces are treated as a single pattern,
 * rather than being split at commas inside the braces.
 * Whitespace around patterns is also trimmed.
 */
export declare const splitPatterns: (patterns?: string) => string[];
//# sourceMappingURL=patternUtils.d.ts.map