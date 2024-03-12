package com.shatteredpixel.shatteredpixeldungeon.spdnet.utils;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class NetLog extends Component implements Signal.Listener<String> {

	private static final int MAX_LINES = 3;

	private static final Pattern PUNCTUATION = Pattern.compile(".*[.,;?! ]$");

	private RenderedTextBlock lastEntry;
	private int lastColor;

	private static ArrayList<Entry> entries = new ArrayList<>();

	public NetLog() {
		super();
		NLog.update.replace(this);

		recreateLines();
	}

	private static ArrayList<String> textsToAdd = new ArrayList<>();

	@Override
	public synchronized void update() {
		int maxLines = SPDSettings.interfaceSize() > 0 ? 5 : 3;
		for (String text : textsToAdd) {
			if (length != entries.size()) {
				clear();
				recreateLines();
			}

			if (text.equals(GLog.NEW_LINE)) {
				lastEntry = null;
				continue;
			}

			int color = CharSprite.DEFAULT;
			if (text.startsWith(GLog.POSITIVE)) {
				text = text.substring(GLog.POSITIVE.length());
				color = CharSprite.POSITIVE;
			} else if (text.startsWith(GLog.NEGATIVE)) {
				text = text.substring(GLog.NEGATIVE.length());
				color = CharSprite.NEGATIVE;
			} else if (text.startsWith(GLog.WARNING)) {
				text = text.substring(GLog.WARNING.length());
				color = CharSprite.WARNING;
			} else if (text.startsWith(GLog.HIGHLIGHT)) {
				text = text.substring(GLog.HIGHLIGHT.length());
				color = CharSprite.NEUTRAL;
			}

			if (lastEntry != null && color == lastColor && lastEntry.nLines < maxLines) {

				String lastMessage = lastEntry.text();
				lastEntry.text(lastMessage.length() == 0 ? text : lastMessage + " " + text);

				entries.get(entries.size() - 1).text = lastEntry.text();

			} else {

				lastEntry = PixelScene.renderTextBlock(text, 6);
				lastEntry.setHightlighting(false);
				lastEntry.hardlight(color);
				lastColor = color;
				add(lastEntry);

				entries.add(new Entry(text, color));

			}

			if (length > 0) {
				int nLines;
				do {
					nLines = 0;
					for (int i = 0; i < length - 1; i++) {
						nLines += ((RenderedTextBlock) members.get(i)).nLines;
					}

					if (nLines > maxLines) {
						RenderedTextBlock r = ((RenderedTextBlock) members.get(0));
						remove(r);
						r.destroy();

						entries.remove(0);
					}
				} while (nLines > maxLines);
				if (entries.isEmpty()) {
					lastEntry = null;
				}
			}
		}

		if (!textsToAdd.isEmpty()) {
			layout();
			textsToAdd.clear();
		}
		super.update();
	}

	private synchronized void recreateLines() {
		for (Entry entry : entries) {
			lastEntry = PixelScene.renderTextBlock(entry.text, 6);
			lastEntry.hardlight(lastColor = entry.color);
			add(lastEntry);
		}
	}

	public synchronized void newLine() {
		lastEntry = null;
	}

	@Override
	public synchronized boolean onSignal(String text) {
		textsToAdd.add(text);
		return false;
	}

	@Override
	protected void layout() {
		float pos = y;
		for (int i = length - 1; i >= 0; i--) {
			RenderedTextBlock entry = (RenderedTextBlock) members.get(i);
			entry.maxWidth((int) width);
			entry.setPos(x, pos - entry.height());
			pos -= entry.height() + 2;
		}
	}

	private static class Entry {
		public String text;
		public int color;

		public Entry(String text, int color) {
			this.text = text;
			this.color = color;
		}
	}

	public static void wipe() {
		entries.clear();
		textsToAdd.clear();
	}
}
