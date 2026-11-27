package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Keys;
import me.bbijabnpobatejb.fancyhealth.compat.Mc;

import java.util.ArrayDeque;
import java.util.Deque;

final class TextField {

    private static final int HISTORY = 100;

    final int max;
    String text = "";
    int cursor;
    int anchor;
    private final Deque<State> undo = new ArrayDeque<>();
    private final Deque<State> redo = new ArrayDeque<>();
    private boolean typing;

    TextField(int max) {
        this.max = max;
    }

    boolean hasSelection() {
        return cursor != anchor;
    }

    int selStart() {
        return Math.min(cursor, anchor);
    }

    int selEnd() {
        return Math.max(cursor, anchor);
    }

    void reset(String s) {
        if (s.equals(text)) return;
        save(false);
        text = s;
        cursor = anchor = s.length();
    }

    void selectAll() {
        anchor = 0;
        cursor = text.length();
        typing = false;
    }

    void moveTo(int i, boolean extend) {
        cursor = Math.max(0, Math.min(text.length(), i));
        if (!extend) anchor = cursor;
        typing = false;
    }

    void selectWord(int i) {
        i = Math.max(0, Math.min(text.length(), i));
        int a = i, b = i;
        while (a > 0 && text.charAt(a - 1) != ' ') a--;
        while (b < text.length() && text.charAt(b) != ' ') b++;
        anchor = a;
        cursor = b;
        typing = false;
    }

    boolean type(int cp) {
        if (cp < 32 || cp == 127) return false;
        if (cp == ' ') {
            int at = selStart();
            boolean before = at == 0 || text.charAt(at - 1) == ' ';
            boolean after = selEnd() < text.length() && text.charAt(selEnd()) == ' ';
            if (before || after) return true;
        }
        boolean merge = typing && cp != ' ' && !hasSelection();
        insert(new String(Character.toChars(cp)), merge);
        typing = cp != ' ';
        return true;
    }

    void paste(String s) {
        insert(s.replaceAll("[\\p{Cntrl}\\s]+", " "), false);
    }

    boolean key(int key, boolean ctrl, boolean shift) {
        if (ctrl && key == Keys.A) {
            selectAll();
        } else if (ctrl && (key == Keys.C || key == Keys.X)) {
            if (hasSelection()) {
                Mc.setClipboard(text.substring(selStart(), selEnd()));
                if (key == Keys.X) replace(selStart(), selEnd(), "");
            }
        } else if (ctrl && key == Keys.V) {
            paste(Mc.clipboard());
        } else if (ctrl && key == Keys.Z && !shift) {
            step(undo, redo);
        } else if (ctrl && (key == Keys.Y || key == Keys.Z)) {
            step(redo, undo);
        } else if (key == Keys.LEFT || key == Keys.RIGHT) {
            int dir = key == Keys.LEFT ? -1 : 1;
            if (hasSelection() && !shift && !ctrl) moveTo(dir < 0 ? selStart() : selEnd(), false);
            else moveTo(ctrl ? word(cursor, dir) : cursor + dir, shift);
        } else if (key == Keys.HOME) {
            moveTo(0, shift);
        } else if (key == Keys.END) {
            moveTo(text.length(), shift);
        } else if (key == Keys.BACKSPACE || key == Keys.DELETE) {
            int dir = key == Keys.BACKSPACE ? -1 : 1;
            if (hasSelection()) replace(selStart(), selEnd(), "");
            else {
                int to = ctrl ? word(cursor, dir) : Math.max(0, Math.min(text.length(), cursor + dir));
                if (to != cursor) replace(Math.min(to, cursor), Math.max(to, cursor), "");
            }
        } else {
            return false;
        }
        return true;
    }

    private int word(int from, int dir) {
        int i = from;
        if (dir < 0) {
            while (i > 0 && text.charAt(i - 1) == ' ') i--;
            while (i > 0 && text.charAt(i - 1) != ' ') i--;
        } else {
            while (i < text.length() && text.charAt(i) != ' ') i++;
            while (i < text.length() && text.charAt(i) == ' ') i++;
        }
        return i;
    }

    private void insert(String s, boolean merge) {
        int room = max - (text.length() - (selEnd() - selStart()));
        if (s.length() > room) s = s.substring(0, Math.max(0, room));
        if (s.isEmpty() && !hasSelection()) return;
        if (!merge) save(false);
        else redo.clear();
        int at = selStart();
        text = text.substring(0, at) + s + text.substring(selEnd());
        cursor = anchor = at + s.length();
    }

    private void replace(int a, int b, String s) {
        save(false);
        text = text.substring(0, a) + s + text.substring(b);
        cursor = anchor = a + s.length();
    }

    private void save(boolean keepTyping) {
        if (undo.size() >= HISTORY) undo.removeLast();
        undo.push(new State(text, cursor, anchor));
        redo.clear();
        typing = keepTyping;
    }

    private void step(Deque<State> from, Deque<State> to) {
        if (from.isEmpty()) return;
        to.push(new State(text, cursor, anchor));
        State s = from.pop();
        text = s.text;
        cursor = s.cursor;
        anchor = s.anchor;
        typing = false;
    }

    private static final class State {
        final String text;
        final int cursor;
        final int anchor;

        State(String text, int cursor, int anchor) {
            this.text = text;
            this.cursor = cursor;
            this.anchor = anchor;
        }
    }
}
