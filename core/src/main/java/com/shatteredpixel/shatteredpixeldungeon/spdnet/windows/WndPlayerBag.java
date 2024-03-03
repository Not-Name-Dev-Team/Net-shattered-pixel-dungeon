package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RightClickMenu;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

public class WndPlayerBag extends WndTabbed_Overrite {

	//only one bag window can appear at a time
	public static Window INSTANCE;

	protected static final int COLS_P = 5;
	protected static final int COLS_L = 5;

	protected static int SLOT_WIDTH_P = 28;
	protected static int SLOT_WIDTH_L = 28;

	protected static int SLOT_HEIGHT_P = 28;
	protected static int SLOT_HEIGHT_L = 28;

	protected static final int SLOT_MARGIN = 1;

	protected static final int TITLE_HEIGHT = 14;

	private ItemSelector selector;

	private int nCols;
	private int nRows;

	private int slotWidth;
	private int slotHeight;

	protected int count;
	protected int col;
	protected int row;

	private static Bag lastBag;
	private static NetHero hero;

	public WndPlayerBag(NetHero hero) {
		this(hero.belongings.backpack, null, hero);
	}

	private WndPlayerBag(Bag bag, ItemSelector selector) {
		this(bag, selector, null);
	}

	private WndPlayerBag(Bag bag, ItemSelector selector, NetHero hero) {

		super();

		if (hero != null) {
			WndPlayerBag.hero = hero;
		}

		if (INSTANCE != null) {
			INSTANCE.hide();
		}
		INSTANCE = this;

		this.selector = selector;

		lastBag = bag;

		slotWidth = PixelScene.landscape() ? SLOT_WIDTH_L : SLOT_WIDTH_P;
		slotHeight = PixelScene.landscape() ? SLOT_HEIGHT_L : SLOT_HEIGHT_P;

		nCols = PixelScene.landscape() ? COLS_L : COLS_P;
		nRows = (int) Math.ceil(25 / (float) nCols); //we expect to lay out 25 slots in all cases

		int windowWidth = slotWidth * nCols + SLOT_MARGIN * (nCols - 1);
		int windowHeight = TITLE_HEIGHT + slotHeight * nRows + SLOT_MARGIN * (nRows - 1);

		if (PixelScene.landscape()) {
			while (slotHeight >= 24 && (windowHeight + 20 + chrome.marginTop()) > PixelScene.uiCamera.height) {
				slotHeight--;
				windowHeight -= nRows;
			}
		} else {
			while (slotWidth >= 26 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width) {
				slotWidth--;
				windowWidth -= nCols;
			}
		}

		placeTitle(bag, windowWidth);

		placeItems(bag);

		resize(windowWidth, windowHeight);

		int i = 1;
		for (Bag b : WndPlayerBag.hero.belongings.getBags()) {
			if (b != null) {
				BagTab tab = new BagTab(b, i++);
				add(tab);
				tab.select(b == bag);
			}
		}

		layoutTabs();
	}

	public static WndPlayerBag lastBag(ItemSelector selector) {

		if (lastBag != null && WndPlayerBag.hero.belongings.backpack.contains(lastBag)) {

			return new WndPlayerBag(lastBag, selector);

		} else {

			return new WndPlayerBag(WndPlayerBag.hero.belongings.backpack, selector);

		}
	}

	public static WndPlayerBag getBag(ItemSelector selector) {
		if (selector.preferredBag() == Belongings.Backpack.class) {
			return new WndPlayerBag(WndPlayerBag.hero.belongings.backpack, selector);

		} else if (selector.preferredBag() != null) {
			Bag bag = WndPlayerBag.hero.belongings.getItem(selector.preferredBag());
			if (bag != null) return new WndPlayerBag(bag, selector);
				//if a specific preferred bag isn't present, then the relevant items will be in backpack
			else return new WndPlayerBag(WndPlayerBag.hero.belongings.backpack, selector);
		}

		return lastBag(selector);
	}

	protected void placeTitle(Bag bag, int width) {

		float titleWidth;
		if (Dungeon.energy == 0) {
			ItemSprite gold = new ItemSprite(ItemSpriteSheet.GOLD, null);
			gold.x = width - gold.width();
			gold.y = (TITLE_HEIGHT - gold.height()) / 2f;
			PixelScene.align(gold);
			add(gold);

			BitmapText amt = new BitmapText(Integer.toString(Dungeon.gold), PixelScene.pixelFont);
			amt.hardlight(TITLE_COLOR);
			amt.measure();
			amt.x = width - gold.width() - amt.width() - 1;
			amt.y = (TITLE_HEIGHT - amt.baseLine()) / 2f - 1;
			PixelScene.align(amt);
			add(amt);

			titleWidth = amt.x;
		} else {

			Image gold = Icons.get(Icons.COIN_SML);
			gold.x = width - gold.width() - 0.5f;
			gold.y = 0;
			PixelScene.align(gold);
			add(gold);

			BitmapText amt = new BitmapText(Integer.toString(Dungeon.gold), PixelScene.pixelFont);
			amt.hardlight(TITLE_COLOR);
			amt.measure();
			amt.x = width - gold.width() - amt.width() - 2f;
			amt.y = 0;
			PixelScene.align(amt);
			add(amt);

			titleWidth = amt.x;

			Image energy = Icons.get(Icons.ENERGY_SML);
			energy.x = width - energy.width();
			energy.y = gold.height();
			PixelScene.align(energy);
			add(energy);

			amt = new BitmapText(Integer.toString(Dungeon.energy), PixelScene.pixelFont);
			amt.hardlight(0x44CCFF);
			amt.measure();
			amt.x = width - energy.width() - amt.width() - 1;
			amt.y = energy.y;
			PixelScene.align(amt);
			add(amt);

			titleWidth = Math.min(titleWidth, amt.x);
		}

		String title = selector != null ? selector.textPrompt() : null;
		RenderedTextBlock txtTitle = PixelScene.renderTextBlock(
				title != null ? Messages.titleCase(title) : Messages.titleCase(bag.name()), 8);
		txtTitle.hardlight(TITLE_COLOR);
		txtTitle.maxWidth((int) titleWidth - 2);
		txtTitle.setPos(
				1,
				(TITLE_HEIGHT - txtTitle.height()) / 2f - 1
		);
		PixelScene.align(txtTitle);
		add(txtTitle);
	}

	protected void placeItems(Bag container) {

		// Equipped items
		Belongings stuff = WndPlayerBag.hero.belongings;
		placeItem(stuff.weapon != null ? stuff.weapon : new Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
		placeItem(stuff.armor != null ? stuff.armor : new Placeholder(ItemSpriteSheet.ARMOR_HOLDER));
		placeItem(stuff.artifact != null ? stuff.artifact : new Placeholder(ItemSpriteSheet.ARTIFACT_HOLDER));
		placeItem(stuff.misc != null ? stuff.misc : new Placeholder(ItemSpriteSheet.SOMETHING));
		placeItem(stuff.ring != null ? stuff.ring : new Placeholder(ItemSpriteSheet.RING_HOLDER));

		int equipped = 5;

		//the container itself if it's not the root backpack
		if (container != WndPlayerBag.hero.belongings.backpack) {
			placeItem(container);
			count--; //don't count this one, as it's not actually inside of itself
		} else if (stuff.secondWep != null) {
			//second weapon always goes to the front of view on main bag
			placeItem(stuff.secondWep);
			equipped++;
		}

		// Items in the bag, except other containers (they have tags at the bottom)
		for (Item item : container.items.toArray(new Item[0])) {
			if (!(item instanceof Bag)) {
				placeItem(item);
			} else {
				count++;
			}
		}

		// Free Space
		while ((count - equipped) < container.capacity()) {
			placeItem(null);
		}
	}

	protected void placeItem(final Item item) {

		count++;

		int x = col * (slotWidth + SLOT_MARGIN);
		int y = TITLE_HEIGHT + row * (slotHeight + SLOT_MARGIN);

		InventorySlot slot = new InventorySlot(item) {
			@Override
			protected void onClick() {
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(WndPlayerBag.hero)) {

					hide();

				} else if (selector != null) {

					hide();
					selector.onSelect(item);

				} else {

					Game.scene().addToFront(new WndUseItem(WndPlayerBag.this, item));

				}
			}

			@Override
			protected void onRightClick() {
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(WndPlayerBag.hero)) {

					hide();

				} else if (selector != null) {

					hide();
					selector.onSelect(item);

				} else {

					RightClickMenu r = new RightClickMenu(item) {
						@Override
						public void onSelect(int index) {
							WndPlayerBag.this.hide();
						}
					};
					parent.addToFront(r);
					r.camera = camera();
					PointF mousePos = PointerEvent.currentHoverPos();
					mousePos = camera.screenToCamera((int) mousePos.x, (int) mousePos.y);
					r.setPos(mousePos.x - 3, mousePos.y - 3);

				}
			}

			@Override
			protected boolean onLongClick() {
				if (selector == null && item.defaultAction() != null) {
					hide();
					QuickSlotButton.set(item);
					return true;
				} else if (selector != null) {
					Game.scene().addToFront(new WndInfoItem(item));
					return true;
				} else {
					return false;
				}
			}
		};
		slot.setRect(x, y, slotWidth, slotHeight);
		add(slot);

		if (item == null || (selector != null && !selector.itemSelectable(item))) {
			slot.enable(false);
		}

		if (++col >= nCols) {
			col = 0;
			row++;
		}

	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey(event) == SPDAction.INVENTORY) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}

	@Override
	public void onBackPressed() {
		if (selector != null) {
			selector.onSelect(null);
		}
		super.onBackPressed();
	}

	@Override
	protected void onClick(Tab tab) {
		hide();
		Window w = new WndPlayerBag(((BagTab) tab).bag, selector);
		if (Game.scene() instanceof GameScene) {
			GameScene.show(w);
		} else {
			Game.scene().addToFront(w);
		}
	}

	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this) {
			INSTANCE = null;
		}
	}

	@Override
	protected int tabHeight() {
		return 20;
	}

	private Image icon(Bag bag) {
		if (bag instanceof VelvetPouch) {
			return Icons.get(Icons.SEED_POUCH);
		} else if (bag instanceof ScrollHolder) {
			return Icons.get(Icons.SCROLL_HOLDER);
		} else if (bag instanceof MagicalHolster) {
			return Icons.get(Icons.WAND_HOLSTER);
		} else if (bag instanceof PotionBandolier) {
			return Icons.get(Icons.POTION_BANDOLIER);
		} else {
			return Icons.get(Icons.BACKPACK);
		}
	}

	private class BagTab extends IconTab {

		private Bag bag;
		private int index;

		public BagTab(Bag bag, int index) {
			super(icon(bag));

			this.bag = bag;
			this.index = index;
		}

		@Override
		public GameAction keyAction() {
			switch (index) {
				case 1:
				default:
					return SPDAction.BAG_1;
				case 2:
					return SPDAction.BAG_2;
				case 3:
					return SPDAction.BAG_3;
				case 4:
					return SPDAction.BAG_4;
				case 5:
					return SPDAction.BAG_5;
			}
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(bag.name());
		}
	}

	public static class Placeholder extends Item {

		public Placeholder(int image) {
			this.image = image;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public boolean isEquipped(Hero hero) {
			return true;
		}
	}

	public abstract static class ItemSelector {
		public abstract String textPrompt();

		public Class<? extends Bag> preferredBag() {
			return null; //defaults to last bag opened
		}

		public abstract boolean itemSelectable(Item item);

		public abstract void onSelect(Item item);
	}
}
