package itdelatrisu.opsu.states;

import com.badlogic.gdx.utils.TimeUtils;

import itdelatrisu.opsu.GameImage;
import itdelatrisu.opsu.Opsu;
import itdelatrisu.opsu.audio.MusicController;
import itdelatrisu.opsu.audio.SoundController;
import itdelatrisu.opsu.audio.SoundEffect;
import itdelatrisu.opsu.beatmap.Beatmap;
import itdelatrisu.opsu.beatmap.BeatmapSetNode;
import itdelatrisu.opsu.beatmap.HitObject;
import itdelatrisu.opsu.beatmap.TimingPoint;
import itdelatrisu.opsu.options.Options;
import itdelatrisu.opsu.options.Options.GameOption;
import itdelatrisu.opsu.ui.Fonts;
import itdelatrisu.opsu.ui.MenuButton;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.ui.animations.AnimationEquation;
import fluddokt.newdawn.slick.state.transition.EasedFadeOutTransition;
import fluddokt.newdawn.slick.state.transition.FadeInTransition;
import fluddokt.opsu.fake.BasicGameState;
import fluddokt.opsu.fake.Color;
import fluddokt.opsu.fake.GameContainer;
import fluddokt.opsu.fake.Graphics;
import fluddokt.opsu.fake.Image;
import fluddokt.opsu.fake.Input;
import fluddokt.opsu.fake.SlickException;
import fluddokt.opsu.fake.StateBasedGame;

public class CalibrateOffsetMenu extends BasicGameState {
	// game-related variables
	private StateBasedGame game;
	private Input input;
	private final int state;
	
	double[] stuff = new double[35];
	int stuffat = 0;
	float avg = 0;
	double[] stuff2 = new double[stuff.length];
	float avg2 = 0;
	float deviation, deviation2;
	
	double rollAvg;
	
	float lowDeviation;
	int passCnt;
	
	double floatingAvg;
	
	private MenuButton logo;
	private BeatmapSetNode focusNode;
	
	boolean useSound = false;

	@Override
	public int getID() { return state; }
	
	public CalibrateOffsetMenu(int state) {
		this.state = state;
	}
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.game = game;
		this.input = container.getInput();
		int width = container.getWidth();
		int height = container.getHeight();
		Image logoImg = GameImage.MENU_LOGO.getImage();
		logo = new MenuButton(logoImg, width / 2, height / 2);
		final int logoAnimationDuration = 350;
		final AnimationEquation logoAnimationEquation = AnimationEquation.IN_OUT_BACK;
		logo.setHoverAnimationDuration(logoAnimationDuration);
		logo.setHoverAnimationEquation(logoAnimationEquation);
		final float logoHoverScale = 1.08f;
		logo.setHoverExpand(logoHoverScale);
		
	}
	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		passCnt = 0;
		lowDeviation = 9999;
		stuffat = 0;
		avg = 0;
		avg2 = 0;
		rollAvg = 0;
		deviation = 0;
		deviation2 = 0;
		for(int i=0; i<stuff.length; i++) {
			stuff[i] = 0;
			stuff2[i] = 0;
		}
	
		SongMenu gameState = (SongMenu) game.getState(Opsu.STATE_SONGMENU);
		focusNode = gameState.getFocusNode();
		System.out.println("Focus:"+focusNode);
		
		HitObject[] h = MusicController.getBeatmap().objects;
		if (h== null)
			return;
		TimingPoint t = MusicController.getLastTimingPoint();
		/*for(int i=0; i<h.length; i++) {
			float as =Math.round((h[i].getTime()-t.getTime())%t.getBeatLength()/t.getBeatLength()*100);
			System.out.println(i+" "+as);
		}*/
		
	}
	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
	}
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = container.getWidth();
		int height = container.getHeight();
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		UI.getBackButton().draw(g);
		
		/*Float position = MusicController.getBeatProgress();
		if (position == null)
			position = 0f;
		else if (position < 0.2f)
			position = AnimationEquation.IN_QUINT.calc(position * 5f);
		else
			position = 1f - AnimationEquation.OUT_QUAD.calc((position - 0.2f) * 1.25f);
		float scale = 1f - position * 0.05f;
		logo.draw(Color.white, scale);
		float ghostScale = logo.getLastScale() / scale * 1.05f;
		Image ghostLogo = GameImage.MENU_LOGO.getImage();//.getScaledCopy(ghostScale);
		ghostLogo.drawCentered(logo.getX(), logo.getY(), Colors.GHOST_LOGO);
		*/
		g.setColor(Color.white);
		
		int x = width/2 + 10;
		int y = 19;
		TimingPoint t = MusicController.getLastTimingPoint();
		//Fonts.DEFAULT.drawString( x, y, MusicController.getBeatProgress()+" "+MusicController.getMeasureProgress());
		y += Fonts.DEFAULT.getLineHeight();
		//Fonts.DEFAULT.drawString( x, y, "low:"+lowDeviation+"");
		y += Fonts.DEFAULT.getLineHeight();
		//Fonts.DEFAULT.drawString( x, y, "dev:"+deviation+"");
		y += Fonts.DEFAULT.getLineHeight();
		//Fonts.DEFAULT.drawString( x, y, "dev2:"+deviation2+"");
		y += Fonts.DEFAULT.getLineHeight();
		//Fonts.DEFAULT.drawString( x, y, "Avg:"+avg);
		y += Fonts.DEFAULT.getLineHeight();
		//Fonts.DEFAULT.drawString( x, y, "Beat:"+avg2+" "+(t!=null?t.getBeatLength():""));
		Fonts.DEFAULT.drawString( x, y, "Tap at the beat until you get an acceptable offset");
		y += Fonts.DEFAULT.getLineHeight();
		Fonts.DEFAULT.drawString( x, y, "Offset:"+Options.getMusicOffset());
		y += Fonts.DEFAULT.getLineHeight();
		Fonts.DEFAULT.drawString( x, y, "Tap Count:"+passCnt);
		y += Fonts.DEFAULT.getLineHeight();
		
		
		if (focusNode != null) {
			Beatmap beatmap = focusNode.getBeatmapSet().get(focusNode.beatmapIndex);
			
			java.util.List<String> str = Fonts.wrap(Fonts.DEFAULT, beatmap.tags, width, false);
			Fonts.loadGlyphs(Fonts.DEFAULT, beatmap.tags);
			int cnt = 0;
			x = 10;
			Fonts.DEFAULT.drawString( x, y, "Random information");
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "id: "+beatmap.beatmapID);
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "setid: "+beatmap.beatmapSetID);
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "WidescreenStoryboard: "+beatmap.widescreenStoryboard+" Letterbox: "+beatmap.letterboxInBreaks);
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "Source: "+beatmap.source);
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "File: "+beatmap.getFile().toString());
			y += Fonts.DEFAULT.getLineHeight();
			Fonts.DEFAULT.drawString( x, y, "Tags: ");
			y += Fonts.DEFAULT.getLineHeight();
			
			/*Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() , "id: "+beatmap.beatmapID, Color.white, Color.black);
			cnt++;
			Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() , "setid: "+beatmap.beatmapSetID, Color.white, Color.black);
			cnt++;
			Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() , "WidescreenStoryboard: "+beatmap.widescreenStoryboard+" Letterbox: "+beatmap.letterboxInBreaks, Color.white, Color.black);
			cnt++;
			Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() , "source: "+beatmap.source, Color.white, Color.black);
			cnt++;
			Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() ,  beatmap.getFile().toString(), Color.white, Color.black);
			cnt++;
			*/
			for(String s: str){
				//Fonts.drawBorderedString(Fonts.DEFAULT,10, 200 + cnt * Fonts.DEFAULT.getLineHeight() ,s ,Color.white, Color.black);
				//cnt ++;
				Fonts.DEFAULT.drawString( x, y, s);
				y += Fonts.DEFAULT.getLineHeight();
			}
		}
		
		g.drawLine(width/2, 0, width/2, height);
		g.setColor(Color.red);
		for(int i=0; i<stuff.length; i++) {
			g.drawLine(
					(float)stuff[i]+width/2,height/2-10+i*5,
					(float)stuff[i]+width/2,height/2+10+i*5
				);
			
		}
		TimingPoint tp = MusicController.getLastTimingPoint();
		double beatLength = 0;
		if(tp != null)
			beatLength = tp.getBeatLength();
		g.setColor(Color.blue);
		for(int i=0; i<stuff.length; i++) {
			g.drawLine(
					(float)(stuff2[i]-beatLength+width/2),height/2-10+i*5,
					(float)(stuff2[i]-beatLength+width/2),height/2+10+i*5
				);
		}
		g.setColor(Color.green);
		g.drawLine(
				(float)avg+width/2,height/2-20,
				(float)avg+width/2,height/2+20
			);
		g.setColor(Color.green);
		g.drawLine(
				(float)floatingAvg+width/2,height/2-20-10,
				(float)floatingAvg+width/2,height/2+20-10
			);
		g.drawLine(
				(float)(rollAvg/passCnt)+width/2,height/2-20-20,
				(float)(rollAvg/passCnt)+width/2,height/2+20-20
			);
		
		
		UI.draw(g);
		
		/*
		int trackPosition = MusicController.getPosition(true);
		TimingPoint t = MusicController.getLastTimingPoint();
		if(t == null)
			return;
		double beatTime = t.getTime();
		double beatLength = t.getBeatLength();
		double t2 = ((trackPosition-beatTime))%beatLength;
		if (t2 > beatLength/2) {
			t2 -= beatLength;
		}
		if (t2 < -beatLength/2) {
			t2 += beatLength;
		}
		double mult = 0.3;
		g.drawLine(width/2, 0, width/2, height);
		double x2 = t2*mult + width/2;
		while (x2>0)
			x2-=beatLength*mult;
		while(x2<width) {
			
			g.drawLine((float)x2, height/2 -10, (float)x2, height/2 +10);
			x2+= beatLength*mult;
		}
		*/
	}
	int beatAt = 0;
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		int width = container.getWidth();
		int height = container.getHeight();
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		UI.update(delta);
		MusicController.loopTrackIfEnded(false);
		UI.getBackButton().hoverUpdate(delta, mouseX, mouseY);
		int trackPosition = MusicController.getPosition(true);
		//*
		TimingPoint t = MusicController.getLastTimingPoint();
		if(t == null)
			return;
		double beatTime = t.getTime();
		double beatLength = t.getBeatLength();
		double t2 = ((trackPosition-beatTime))/beatLength;
		if (useSound) {
			if(beatAt != (int)t2)
				SoundController.playSound(SoundEffect.MENUCLICK);
		}
		beatAt = (int)t2;
		//System.out.println(beatAt);
		//*/
		//System.out.println(TimeUtils.millis());
		//long t = TimeUtils.nanoTime()/1000000;
		//long t = TimeUtils.millis();
		
		//System.out.println(t-a);
		//a = t;
	}
	long a;
	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			back();
			break;
		case Input.KEY_ADD:
			avg = 1;
			sync();
			break;
		case Input.KEY_MINUS:
			avg = -1;
			sync();
			break;
		case Input.KEY_0:
			useSound = !useSound;
			break;
		
		case Input.KEY_ENTER:
			sync();
			break;
		default:
			time();
		}
		
	}
	@Override
	public void mousePressed(int button, int x, int y) {
		// back
		if (UI.getBackButton().contains(x, y)) {
			back();
			return;
		}
		if (button == Input.MOUSE_MIDDLE_BUTTON){
			sync();
		}
		time();
	}
	long lastPress = 0;
	private void time() {
		int trackPosition = MusicController.getPosition(true);
		long thisTime = TimeUtils.millis();//trackPosition;
		long dtime = thisTime - lastPress;
		lastPress = thisTime;
		TimingPoint t = MusicController.getLastTimingPoint();
		if(t==null)
			return;
		
		double beatTime = t.getTime();
		double beatLength = t.getBeatLength() * 100.0;
		double t2 = ((trackPosition-beatTime)*100)%beatLength;
		if (t2 > beatLength/2) {
			t2 -= beatLength;
		}
		if (t2 < -beatLength/2) {
			t2 += beatLength;
		}
		t2/=100;
		stuff[stuffat] = t2;
		rollAvg += t2;
		
		final double mult = 0.1;
		final double mult2 = 1-mult;
		floatingAvg = t2 * mult + floatingAvg * mult2;
		
		stuff2[stuffat] = dtime;
		stuffat = (stuffat+1)%stuff.length;
		//System.out.println(beatTime+" "+beatLength/100+" "+MusicController.getBeatProgress()+" "+t2+" "+dtime);
		avg = 0;
		avg2 = 0;
		for(int i=0; i<stuff.length; i++) {
			avg += stuff[i];
			avg2 += stuff2[i];
		}
		avg/=stuff.length;
		avg2/=stuff2.length;
		deviation = 0;
		deviation2 = 0;
		for (int i=0; i<stuff.length; i++) {
			double dif = stuff[i]-avg;
			deviation += dif * dif;
			double dif2 = stuff2[i]-avg2;
			deviation2 += dif2 * dif2;
			
		}
		deviation/=stuff.length;
		deviation = (float) Math.sqrt(deviation);
		deviation2/=stuff.length;
		deviation2 = (float) Math.sqrt(deviation2);
		
		passCnt++;
		if (deviation < 50) {
			if (passCnt >= stuff.length) {
				//if(deviation2+deviation < lowDeviation) {
					lowDeviation = deviation2+deviation;
					sync();
				//}
			} 
		} else {
			//passCnt = 0;
		}
		
	}

	private void sync() {
		//int off = (int)avg;
		int off = (int) (rollAvg / passCnt);
		rollAvg -= off * passCnt;
		for (int i=0; i<stuff.length; i++)
			stuff[i]-=off;
		GameOption.MUSIC_OFFSET.setValue(Options.getMusicOffset()-off);
	}
	private void back() {
		SoundController.playSound(SoundEffect.MENUBACK);
		game.enterState(Opsu.STATE_SONGMENU, new EasedFadeOutTransition(), new FadeInTransition());
	}
	
}
