var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { getVersion } from '../../core/file/packageJsonParse.js';
import { logger } from '../../shared/logger.js';
export const runVersionAction = () => __awaiter(void 0, void 0, void 0, function* () {
    const version = yield getVersion();
    logger.log(version);
});
//# sourceMappingURL=versionAction.js.map