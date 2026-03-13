package cc.allyapps.pastely.websocket.model;

public class CursorPosition {
    private int line;
    private int column;
    private Selection selection;

    public CursorPosition() {}

    public CursorPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    public static class Selection {
        private CursorPosition start;
        private CursorPosition end;

        public Selection() {}

        public Selection(CursorPosition start, CursorPosition end) {
            this.start = start;
            this.end = end;
        }

        public CursorPosition getStart() {
            return start;
        }

        public void setStart(CursorPosition start) {
            this.start = start;
        }

        public CursorPosition getEnd() {
            return end;
        }

        public void setEnd(CursorPosition end) {
            this.end = end;
        }
    }
}
