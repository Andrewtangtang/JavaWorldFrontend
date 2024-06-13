package com.javaworld.clientapp;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class CodeEditor extends StackPane {
    public final WebView webview = new WebView();

    private String editingCode;

    private String applyEditingTemplate() {
        return """
                <!doctype html>
                <html>
                <head>
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/codemirror.min.css"/>
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/theme/ayu-dark.min.css"/>
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/fold/foldgutter.min.css"/>
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/hint/show-hint.min.css"/>\
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:ital,wght@0,100..800;1,100..800&family=Noto+Sans+TC&display=swap" rel="stylesheet">
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/codemirror.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/mode/clike/clike.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/selection/active-line.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/edit/matchbrackets.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/edit/closebrackets.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/fold/foldcode.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/fold/foldgutter.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/fold/brace-fold.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/fold/comment-fold.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/comment/comment.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/comment/continuecomment.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/hint/show-hint.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/hint/anyword-hint.min.js"></script>
                    <style>
                        html,body,form{margin:0;width:100%;height:100%;font-size:18px;}
                        .CodeMirror{height:100%;font-family:"JetBrains Mono",monospace;}
                    </style>
                </head>
                <body>
                    <form><textarea id="code" name="code">""" + editingCode + """
                </textarea></form>
                    <script>var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
                        lineNumbers:true, styleActiveLine:true, autoCloseBrackets:true, matchBrackets:true, hintOptions:{completeSingle:false}, foldGutter:true, gutters:['CodeMirror-linenumbers','CodeMirror-foldgutter'], indentUnit:4, mode:'text/x-java', theme:'ayu-dark',
                        extraKeys: {'Ctrl-/': function(){editor.execCommand('toggleComment');}}
                    });
                    editor.on("keyup", function(cm, e) {
                        if (e.keyCode > 33 && e.keyCode < 127 && !e.ctrlKey && !e.altKey) editor.execCommand("autocomplete");
                    });
                    CodeMirror.commands.autocomplete = function(cm) {
                        cm.showHint({hint: function(cm,d) {var a=CodeMirror.hint.anyword(cm,d); var b=CodeMirror.hint.auto.resolve(cm,d)(cm); if(a&&b)a.list.push(...b.list);else if(b) return b; return a;}});
                    };
                </script>
                </body>
                </html>""";
    }

    public void setCode(String newCode) {
        this.editingCode = newCode;
        webview.getEngine().loadContent(applyEditingTemplate());
    }

    public String getCodeAndSnapshot() {
        this.editingCode = (String) webview.getEngine().executeScript("editor.getValue();");
        return editingCode;
    }

    public void revertEdits() {
        setCode(editingCode);
    }

    public CodeEditor() {
        this.editingCode = """
                import com.javaworld.adapter.PlayerApplication;
                import com.javaworld.adapter.Self;
                import com.almasb.fxgl.core.math.Vec2;

                public class Main extends PlayerApplication {
                    @Override
                    public void init(Self self) {
                        console.println("Init");
                        self.moveTo(new Vec2(0,10));
                    }

                    @Override
                    public void gameUpdate(Self self) {
                        console.println(self.getWorld().getWorldTime());
                    }
                }""";
        webview.getEngine().loadContent(applyEditingTemplate());
        this.getChildren().add(webview);

    }
}
