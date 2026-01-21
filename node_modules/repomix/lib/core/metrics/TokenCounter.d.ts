import { type TiktokenEncoding } from 'tiktoken';
export declare class TokenCounter {
    private encoding;
    constructor(encodingName: TiktokenEncoding);
    countTokens(content: string, filePath?: string): number;
    free(): void;
}
//# sourceMappingURL=TokenCounter.d.ts.map