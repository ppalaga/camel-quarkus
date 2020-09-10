const Opal = global.Opal;

module.exports.register = function(registry, { file, contentCatalog }) {

    registry.blockMacro(function() {
        var self = this
        self.named('cq-reference-table')
        self.process(function(parent, target, attrs) {
            const dir = file.asciidoc.attributes.docdir + "/" + attrs.extensionsDirectory
            console.log("dir = ", dir);
            const extensionPages = contentCatalog.findBy({ family: 'page' })
                .filter((page) => (page.asciidoc.attributes.docdir === dir && page.asciidoc.attributes['cq-since']));
            extensionPages.sort(compare)

            const table = parent.blocks[parent.blocks.length - 1];
            if (table.context !== 'table') {
                console.warn(`cq-reference-table block macro must follow a table, not ${table.context}`)
                return
            }
            const rows = table.rows.body;
            const columns = table.columns;
            for (page of extensionPages) {
                //console.log(JSON.stringify(page.src, null, 2));
                var i = 0;
                const nativeSupported = page.asciidoc.attributes['cq-native-supported'] === 'true';
                const targetRuntime = nativeSupported ? "Native" : "JVM";
                const row = [
                    Opal.Asciidoctor.Table.Cell.$new(
                        columns[i++],
                        `xref:${page.src.version}@${page.src.component}:${page.src.module}:${page.src.relative}[]`),
                    Opal.Asciidoctor.Table.Cell.$new(columns[i++], page.asciidoc.attributes['cq-artifact-id']),
                    Opal.Asciidoctor.Table.Cell.$new(
                        columns[i++],
                        `[.camel-element-${targetRuntime}]##${targetRuntime}##\n${page.asciidoc.attributes['cq-status']}`),
                    Opal.Asciidoctor.Table.Cell.$new(columns[i++], page.asciidoc.attributes['cq-since']),
                    Opal.Asciidoctor.Table.Cell.$new(columns[i++], page.asciidoc.attributes['cq-description'])
                ];
                rows.push(row);
            }
        })
    })
}

const compare = (f1, f2) => {
  console.log(JSON.stringify(f1, null, 2));
  const t1 = f1.asciidoc.doctitle.toLowerCase()
  const t2 = f2.asciidoc.doctitle.toLowerCase()
  return t1 < t2 ? -1 : t1 > t2 ? 1 : 0
}
