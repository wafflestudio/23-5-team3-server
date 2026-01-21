import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
export declare const createMcpServer: () => Promise<McpServer>;
type Dependencies = {
    processExit?: (code?: number) => never;
};
export declare const runMcpServer: (deps?: Dependencies) => Promise<void>;
export {};
//# sourceMappingURL=mcpServer.d.ts.map