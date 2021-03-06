/*
 Copyright (C) 1997-2001 Id Software, Inc.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
/* Modifications
   Copyright 2003-2004 Bytonic Software
   Copyright 2010 Google Inc.
*/
package jake2.client;

import static jake2.qcommon.Defines.NUM_CON_TIMES;
import static jake2.qcommon.Defines.ca_active;
import static jake2.qcommon.Defines.key_console;
import static jake2.qcommon.Defines.key_game;
import static jake2.qcommon.Defines.key_menu;
import static jake2.qcommon.Defines.key_message;
import static jake2.qcommon.Globals.chat_buffer;
import static jake2.qcommon.Globals.chat_bufferlen;
import static jake2.qcommon.Globals.chat_team;
import static jake2.qcommon.Globals.cls;
import static jake2.qcommon.Globals.con;
import static jake2.qcommon.Globals.con_notifytime;
import static jake2.qcommon.Globals.edit_line;
import static jake2.qcommon.Globals.key_linepos;
import static jake2.qcommon.Globals.key_lines;
import static jake2.qcommon.Globals.re;
import static jake2.qcommon.Globals.viddef;

import jake2.game.Cmd;
import jake2.qcommon.Cbuf;
import jake2.qcommon.Com;
import jake2.qcommon.Cvar;
import jake2.qcommon.Defines;
import jake2.qcommon.Globals;
import jake2.qcommon.xcommand_t;
import jake2.util.Vargs;

import java.util.Arrays;

/**
 * Console
 */
public final class Console {

    public static xcommand_t ToggleConsole_f = new xcommand_t() {
        public void execute() {
            SCR.EndLoadingPlaque(); // get rid of loading plaque

            if (Globals.cl.attractloop) {
                Cbuf.AddText("killserver\n");
                return;
            }

            if (Globals.cls.state == Defines.ca_disconnected) {
                // start the demo loop again
                Cbuf.AddText("d1\n");
                return;
            }

            Key.ClearTyping();
            Console.ClearNotify();

            if (Globals.cls.key_dest == Defines.key_console) {
                Menu.ForceMenuOff();
                Cvar.Set("paused", "0");
            } else {
                Menu.ForceMenuOff();
                Globals.cls.key_dest = Defines.key_console;

                if (Cvar.VariableValue("maxclients") == 1
                        && Globals.server_state != 0)
                    Cvar.Set("paused", "1");
            }
        }
    };

    public static xcommand_t Clear_f = new xcommand_t() {
        public void execute() {
            Arrays.fill(Globals.con.text, (byte) ' ');
        }
    };

    /**
     *  
     */
    public static void Init() {
        Globals.con.linewidth = -1;

        CheckResize();

        Com.Printf("Console initialized.\n");

        //
        // register our commands
        //
        Globals.con_notifytime = Cvar.Get("con_notifytime", "3", 0);

        Cmd.AddCommand("toggleconsole", ToggleConsole_f);
        Cmd.AddCommand("togglechat", ToggleChat_f);
        Cmd.AddCommand("messagemode", MessageMode_f);
        Cmd.AddCommand("messagemode2", MessageMode2_f);
        Cmd.AddCommand("clear", Clear_f);
        Globals.con.initialized = true;
    }

    /**
     * If the line width has changed, reformat the buffer.
     */
    public static void CheckResize() {
        int width = (Globals.viddef.width >> 3) - 2;
        if (width > Defines.MAXCMDLINE) width = Defines.MAXCMDLINE;

        if (width == Globals.con.linewidth)
            return;

        if (width < 1) { // video hasn't been initialized yet
            width = 38;
            Globals.con.linewidth = width;
            Globals.con.totallines = Defines.CON_TEXTSIZE
                    / Globals.con.linewidth;
            Arrays.fill(Globals.con.text, (byte) ' ');
        } else {
            int oldwidth = Globals.con.linewidth;
            Globals.con.linewidth = width;
            int oldtotallines = Globals.con.totallines;
            Globals.con.totallines = Defines.CON_TEXTSIZE
                    / Globals.con.linewidth;
            int numlines = oldtotallines;

            if (Globals.con.totallines < numlines)
                numlines = Globals.con.totallines;

            int numchars = oldwidth;

            if (Globals.con.linewidth < numchars)
                numchars = Globals.con.linewidth;

            byte[] tbuf = new byte[Defines.CON_TEXTSIZE];
            System
                    .arraycopy(Globals.con.text, 0, tbuf, 0,
                            Defines.CON_TEXTSIZE);
            Arrays.fill(Globals.con.text, (byte) ' ');

            for (int i = 0; i < numlines; i++) {
                for (int j = 0; j < numchars; j++) {
                    Globals.con.text[(Globals.con.totallines - 1 - i)
                            * Globals.con.linewidth + j] = tbuf[((Globals.con.current
                            - i + oldtotallines) % oldtotallines)
                            * oldwidth + j];
                }
            }

            Console.ClearNotify();
        }

        Globals.con.current = Globals.con.totallines - 1;
        Globals.con.display = Globals.con.current;
    }

    public static void ClearNotify() {
        int i;
        for (i = 0; i < Defines.NUM_CON_TIMES; i++)
            Globals.con.times[i] = 0;
    }

    /*
     * ================ Con_ToggleChat_f ================
     */
    static xcommand_t ToggleChat_f = new xcommand_t() {
        public void execute() {
            Key.ClearTyping();

            if (cls.key_dest == key_console) {
                if (cls.state == ca_active) {
                    Menu.ForceMenuOff();
                    cls.key_dest = key_game;
                }
            } else
                cls.key_dest = key_console;

            ClearNotify();
        }
    };

    /*
     * ================ Con_MessageMode_f ================
     */
    static xcommand_t MessageMode_f = new xcommand_t() {
        public void execute() {
            chat_team = false;
            cls.key_dest = key_message;
        }
    };

    /*
     * ================ Con_MessageMode2_f ================
     */
    static xcommand_t MessageMode2_f = new xcommand_t() {
        public void execute() {
            chat_team = true;
            cls.key_dest = key_message;
        }
    };

    /*
     * =============== Con_Linefeed ===============
     */
    static void Linefeed() {
        Globals.con.x = 0;
        if (Globals.con.display == Globals.con.current)
            Globals.con.display++;
        Globals.con.current++;
        int i = (Globals.con.current % Globals.con.totallines)
                * Globals.con.linewidth;
        int e = i + Globals.con.linewidth;
        while (i++ < e)
            Globals.con.text[i] = ' ';
    }

    /*
     * ================ Con_Print
     * 
     * Handles cursor positioning, line wrapping, etc All console printing must
     * go through this in order to be logged to disk If no console is visible,
     * the text will appear at the top of the game window ================
     */
    private static int cr;

    public static void Print(String txt) {
        int y;
        int c, l;
        int mask;
        int txtpos = 0;

        if (!con.initialized)
            return;

        if (txt.charAt(0) == 1 || txt.charAt(0) == 2) {
            mask = 128; // go to colored text
            txtpos++;
        } else
            mask = 0;

        while (txtpos < txt.length()) {
            c = txt.charAt(txtpos);
            // count word length
            for (l = 0; l < con.linewidth && l < (txt.length() - txtpos); l++)
                if (txt.charAt(l + txtpos) <= ' ')
                    break;

            // word wrap
            if (l != con.linewidth && (con.x + l > con.linewidth))
                con.x = 0;

            txtpos++;

            if (cr != 0) {
                con.current--;
                cr = 0;
            }

            if (con.x == 0) {
                Console.Linefeed();
                // mark time for transparent overlay
                if (con.current >= 0)
                    con.times[con.current % NUM_CON_TIMES] = cls.realtime;
            }

            switch (c) {
            case '\n':
                con.x = 0;
                break;

            case '\r':
                con.x = 0;
                cr = 1;
                break;

            default: // display character and advance
                y = con.current % con.totallines;
                con.text[y * con.linewidth + con.x] = (byte) (c | mask | con.ormask);
                con.x++;
                if (con.x >= con.linewidth)
                    con.x = 0;
                break;
            }
        }
    }

    /*
     * ============== Con_CenteredPrint ==============
     */
    static void CenteredPrint(String text) {
        int l = text.length();
        l = (con.linewidth - l) / 2;
        if (l < 0)
            l = 0;

        StringBuffer sb = new StringBuffer(1024);
        for (int i = 0; i < l; i++)
            sb.append(' ');
        sb.append(text);
        sb.append('\n');

        sb.setLength(1024);

        Console.Print(sb.toString());
    }

    /*
     * ==============================================================================
     * 
     * DRAWING
     * 
     * ==============================================================================
     */

    /*
     * ================ Con_DrawInput
     * 
     * The input line scrolls horizontally if typing goes beyond the right edge
     * ================
     */
    static void DrawInput() {
        int i;
        byte[] text;
        int start = 0;

        if (cls.key_dest == key_menu)
            return;
        if (cls.key_dest != key_console && cls.state == ca_active)
            return; // don't draw anything (always draw if not active)

        text = key_lines[edit_line];

        // add the cursor frame
        text[key_linepos] = (byte) (10 + ((int) (cls.realtime >> 8) & 1));

        // fill out remainder with spaces
        for (i = key_linepos + 1; i < con.linewidth; i++)
            text[i] = ' ';

        // prestep if horizontally scrolling
        if (key_linepos >= con.linewidth)
            start += 1 + key_linepos - con.linewidth;

        // draw it
        //		y = con.vislines-16;
        re.DrawString(1, con.vislines - 22, text, 0, con.linewidth);

        // remove cursor
        key_lines[edit_line][key_linepos] = 0;
    }

    /*
     * ================ Con_DrawNotify
     * 
     * Draws the last few lines of output transparently over the game top
     * ================
     */
    static void DrawNotify() {
        int x, v;
        int text;
        int i;
        int time;
        String s;
        int skip;

        v = 0;
        for (i = con.current - NUM_CON_TIMES + 1; i <= con.current; i++) {
            if (i < 0)
                continue;

            time = (int) con.times[i % NUM_CON_TIMES];
            if (time == 0)
                continue;

            time = (int) (cls.realtime - time);
            if (time > con_notifytime.value * 1000)
                continue;

            text = (i % con.totallines) * con.linewidth;

            re.DrawString(1 << 3, v, con.text, text, con.linewidth);

            v += 8;
        }

        if (cls.key_dest == key_message) {
            if (chat_team) {
                re.DrawString(8, v, "say_team:");
                skip = 11;
            } else {
              re.DrawString(8, v, "say:");
                skip = 5;
            }

            s = chat_buffer;
            if (chat_bufferlen > (viddef.width >> 3) - (skip + 1))
                s = s.substring(chat_bufferlen
                        - ((viddef.width >> 3) - (skip + 1)));

            re.DrawString(skip << 3, v, s);
            x = s.length();
            re.DrawChar((x + skip) << 3, v, (int) (10 + ((cls.realtime >> 8) & 1)));

            v += 8;
        }

        if (v != 0) {
            SCR.AddDirtyPoint(0, 0);
            SCR.AddDirtyPoint(viddef.width - 1, v);
        }
    }

    /*
     * ================ Con_DrawConsole
     * 
     * Draws the console with the solid background ================
     */
    static void DrawConsole(float frac) {
        int i, x, y;
        int rows;
        int row;
        int lines;
        String version;

        lines = (int) (viddef.height * frac);
        if (lines <= 0)
            return;

        if (lines > viddef.height)
            lines = viddef.height;

        // draw the background
        re.DrawStretchPic(0, -viddef.height + lines, viddef.width,
                viddef.height, "conback");
        SCR.AddDirtyPoint(0, 0);
        SCR.AddDirtyPoint(viddef.width - 1, lines - 1);

        version = Com.sprintf("v%4.2f", new Vargs(1).add(Defines.VERSION));
        re.DrawString(viddef.width - 44, lines - 12, version);

        // draw the text
        con.vislines = lines;

        rows = (lines - 22) >> 3; // rows of text to draw

        y = lines - 30;

        // draw from the bottom up
        if (con.display != con.current) {
            // draw arrows to show the buffer is backscrolled
            re.DrawString(8, y, "^^^^");

            y -= 8;
            rows--;
        }

        row = con.display;
        for (i = 0; i < rows; i++, y -= 8, row--) {
            if (row < 0)
                break;
            if (con.current - row >= con.totallines)
                break; // past scrollback wrap point

            int first = (row % con.totallines) * con.linewidth;
            re.DrawString(8, y, con.text, first, con.linewidth);
        }

        // draw the input prompt, user text, and cursor if desired
        DrawInput();
    }
}
