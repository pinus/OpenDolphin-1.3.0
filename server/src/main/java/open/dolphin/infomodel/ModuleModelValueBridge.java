package open.dolphin.infomodel;

import open.dolphin.util.ModelUtils;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/// ModuleModel の beanBytes(byte[]) からテキストを取り出すユーティリティ.
///
/// @author masuda, Masuda Naika
/// @author pns
public class ModuleModelValueBridge implements ValueBridge<ModuleModel, String> {

    @Override
    public String toIndexedValue(ModuleModel mm, ValueBridgeToIndexedValueContext context) {
        // 引数の具体的な中身：
        // 1. mm   : コレクションから取り出された「ModuleModel」のインスタンスが1つずつ渡る
        // 2. context : インデックス作成時のコンテキスト情報（インデックス対象のエンティティなど）が入る
        byte[] beanBytes = mm.getBeanBytes();
        if (beanBytes == null) { return ""; }

        InfoModel im = (InfoModel) ModelUtils.xmlDecode(beanBytes);
        if (im == null) { return ""; }

        if (im instanceof ProgressCourse progressCourse) {
            String xml = progressCourse.getFreeText();
            return ModelUtils.extractText(xml);
        } else {
            return im.toString();
        }
    }
}
