export interface FileManipulator {
    removeComments(content: string): string;
    removeEmptyLines(content: string): string;
}
export declare const getFileManipulator: (filePath: string) => FileManipulator | null;
//# sourceMappingURL=fileManipulate.d.ts.map