package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.SPDNetConfig;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.NetIcons;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.SPDNetChrome;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class NetWindow extends Window {
	public NetWindow(int width, int height) {
		super(width, height, SPDNetChrome.get(SPDNetChrome.Type.WINDOW));
	}

	public NetWindow() {
		super(0, 0, SPDNetChrome.get(SPDNetChrome.Type.WINDOW));
	}

	public static void message(Image i, String title, String message) {
		Game.runOnRenderThread(() -> ShatteredPixelDungeon.scene().add(new WndMessage(i, title, message)));
	}

	public static void message(String title, String message) {
		message(NetIcons.get(NetIcons.GLOBE), title, message);
	}

	public static void message(String message) {
		message(NetIcons.get(NetIcons.GLOBE), "Server Message", message);
	}

	public static void error(String message) {
		message(NetIcons.get(NetIcons.ALERT), Messages.get(NetWindow.class, "error"), message);
	}

	//对于一些正在开发的功能显示它
	public static void dev(String message) {
		message(NetIcons.get(NetIcons.GLOBE), Messages.get(NetWindow.class, "dev"), message);
	}

	//提示文本
	public static void info(String message) {
		message(Icons.get(Icons.INFO), Messages.get(NetWindow.class, "info"), message);
	}

	public static void error(String title, String message) {
		message(NetIcons.get(NetIcons.ALERT), title, message);
	}

	public static void runWindow(Window w) {
		Game.runOnRenderThread(() -> ShatteredPixelDungeon.scene().add(w));
	}

	public static void showServerInfo() {
		show(new WndServerInfo());
	}

	public static void showKeyInput() {
		Game.runOnRenderThread(() -> ShatteredPixelDungeon.scene().add(new WndTextInput("输入你的Key", null, SPDNetConfig.getKey(), 30, false, "确定", "取消") {
			@Override
			public void onSelect(boolean positive, String text) {
				if (positive) {
					SPDNetConfig.setKey(text);
					Net.destroySocket();
				}
			}
		}));
	}

	public static void init(String motd, long seed) {
		show(new WndMotd(motd, seed));
	}

	public static void show(Window w) {
		Game.runOnRenderThread(() -> ShatteredPixelDungeon.scene().add(w));
	}

}
