package Model;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import Configuration.ApplicationConfiguration;
import View.*;
import Patterns.Chain.AbstractGameLogger;
import Patterns.Chain.EagleLogger;
import Patterns.Chain.SharkAttackLogger;
import Patterns.Chain.SharkLogger;
import Patterns.Command.UndoRedoManager;
import Patterns.Composite.CompositeEagles;
import Patterns.Composite.CompositeSharks;
import Patterns.Decorator.EagleDecorator;
import Patterns.Decorator.SharkDecorator;
import Patterns.State.*;
import Patterns.State.Context.GameStatus;



public class Model 
{	
	private List<Tile> tiles;
	private ArrayList<SharkDecorator> sharks;
	private ArrayList<EagleDecorator> eagles;
	private ApplicationConfiguration applicationConfiguration;
	private boolean loadingGame;
	private Context context;
	private GameState gameState;
	private AbstractGameLogger loggerChain ;
	private UndoRedoManager undoRedoManager;
	private Tile attacker;
	
	private CompositeEagles EglesList = new CompositeEagles("Eagles",0,0);
	
	public String getEglesList() {
		List<String> list = new ArrayList<String>();
		for (CompositeEagles egles : EglesList.getSubordinates()) {
			list.add(egles.toString());
	      }	
		return String.valueOf(list);
	}


	private CompositeSharks SharksList = new CompositeSharks("Sharks",0,0);
	public String getSharksList() {
		List<String> list = new ArrayList<String>();
		for (CompositeSharks sharks : SharksList.getSubordinates()) {
			list.add(sharks.toString());
	      }	
		return String.valueOf(list);
	}
	
	public UndoRedoManager getUndoRedoManager() {
		return undoRedoManager;
	}


	public GameState getGameState() {
		return gameState;
	}


	public AbstractGameLogger getLoggerChain() {
		return loggerChain;
	}

	private static AbstractGameLogger getChainOfLogger() {
		  AbstractGameLogger sharkLogger = new SharkLogger(AbstractGameLogger.SHARK);
	      AbstractGameLogger sharkAttackLogger = new SharkAttackLogger(AbstractGameLogger.SHAEKATTACK);
	      AbstractGameLogger eagleLogger = new EagleLogger(AbstractGameLogger.EAGLE);
	      AbstractGameLogger eagleAttackLogger = new EagleLogger(AbstractGameLogger.EAGLEATTACK);

	      sharkLogger.setNextLogger(sharkAttackLogger);
	      sharkAttackLogger.setNextLogger(eagleLogger);
	      eagleLogger.setNextLogger(eagleAttackLogger);
	      
	      return sharkLogger;
	}
	
	public Model() {
		super();
		applicationConfiguration = new ApplicationConfiguration();
		loadingGame = false;
		context = new Context();
		gameState = new GameState();
		loggerChain = getChainOfLogger();		
		undoRedoManager = new UndoRedoManager();
	}

	public void setImageToTile(Tile tileItem,String imageName) {
		try
		{
			Image img = ImageIO.read(new FileInputStream(String.format("resources%s%s%s%s", File.separator, "images",File.separator, imageName + ".jpg")));
			tileItem.setIcon(new ImageIcon(img.getScaledInstance(img.getWidth(null), img.getHeight(null), Image.SCALE_SMOOTH)));
		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void checkLoadingGame() {
		JFrame frame = new JFrame();
		String[] options = new String[2];
		options[0] = new String("Agree");
		options[1] = new String("Disagree");
		
		if(applicationConfiguration.getTilesList().size() > 0)
			loadingGame = 
				JOptionPane.showOptionDialog(frame.getContentPane(),"Do you want to loading game!","Question...", 0,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]) == 0;
		if(!loadingGame)
			applicationConfiguration.SetDefaultApplicationConfiguration();
	}
	
	private void loadGame() {
		int item = 0;
		for(int x = 0;x< applicationConfiguration.getBoardRows();x++)
			for(int y = 0;y<applicationConfiguration.getBoardColumns();y++)
			{
				String name = applicationConfiguration.getTilesList().get(item);
				Tile tile = new Tile(name,x+1,y+1);
				setImageToTile(tile,tile.getAttribute());
				if(isContaingEagle(name)) {
					ChangeBehaviour itemEgale = new ChangeBehaviour(eagles(),tile);
					itemEgale.selectItem(tile.getAttribute());
				}
				if(isContaingShark(name)) {
					ChangeBehaviour itemShark = new ChangeBehaviour(sharks(),tile);
					itemShark.selectItem(tile.getAttribute());
				}
				tiles.add(tile);
				item++;
			}
	}
	
	
	public void initModel(int boardRows, int boardColumns)
	{
		tiles = new ArrayList<>();
		
		sharks = new ArrayList<SharkDecorator>();
		sharks.add(new SharkDecorator("white shark"));
		sharks.add(new SharkDecorator("blue shark"));
		sharks.add(new SharkDecorator("tiger shark"));
		for(int i=0; i < sharks.size(); i++) {
			CompositeSharks Sharks = new CompositeSharks(
					sharks.get(i).getName(), 
					sharks.get(i).getLife(),
					sharks.get(i).getLifeAbility());
			SharksList.add(Sharks);
		}
		
		eagles = new ArrayList<EagleDecorator>();
		eagles.add(new EagleDecorator("Bateleur"));
		eagles.add(new EagleDecorator("Bald"));
		eagles.add(new EagleDecorator("Black"));
		
		for(int i=0; i < sharks.size(); i++) {
			CompositeEagles Egles = new CompositeEagles(
					eagles.get(i).getName(), 
					eagles.get(i).getLife(),
					eagles.get(i).getLifeAbility());
			EglesList.add(Egles);
		}

		context.setGameState(Enum.valueOf(GameStatus.class, applicationConfiguration.getGameStatus()));
		if(loadingGame) {
			loadGame();
			return;
		}
		
		for(int x = 0;x<boardRows;x++)
			for(int y = 0;y<boardColumns;y++)
			{
				int[] islandSet = {0,1};
				int rand = (int)(Math.random() * islandSet.length - 0.5);
				Tile tile = new Tile("",x+1,y+1);
				switch(islandSet[rand]) {
				case 0:
					tile.setOcean();
					break;
				case 1:
					tile.setIsland();
					break;
				}
				setImageToTile(tile,tile.getAttribute());

				tiles.add(tile);
			}
		//Arrange eagle at the top left of the board
		ArrayList<Integer> numbers = new ArrayList<Integer>();  
		ArrayList<Integer> numberList = new ArrayList<Integer>();
		for(int x = 0;x<3;x++) {
			//Generate random number for different location
			Random randomGenerator = new Random();
			while (numbers.size() < 3) {

			    int random = randomGenerator .nextInt(boardRows);
			    if (!numbers.contains(random)) {
			        numbers.add(random);
			        }
			}
			tiles.get(numbers.get(x)).setEagle();
			tiles.get(numbers.get(x)).setName(eagles.get(x).getName());
			tiles.get(numbers.get(x)).setAttribute(eagles.get(x).getName());
			new ChangeBehaviour(eagles(),tiles.get(numbers.get(x)));
			setImageToTile(tiles.get(numbers.get(x)),eagles.get(x).getName());
		}
		//Arrange sharks at the bottom of the board
		for(int x = 0;x<3;x++) {
			numbers.clear();  
			Random randomGenerator = new Random();
			while (numbers.size() < 3) {

			    int random = randomGenerator .nextInt(9);
			    if (!numbers.contains(random) && !numberList.contains(random)) {
			        numbers.add(random);
			    }
			}
			int location = tiles.size() - numbers.get(x) - 1;
			tiles.get(location).setShark();
			tiles.get(location).setName(sharks.get(x).getName());
			tiles.get(location).setAttribute(sharks.get(x).getName());
			new ChangeBehaviour(sharks(),tiles.get(x));
			setImageToTile(tiles.get(location),sharks.get(x).getName());		
			numberList.add(numbers.get(0));
			numberList.add(numbers.get(1));
			numberList.add(numbers.get(2));
		}
	}

	
	public List<Tile> getTiles() {
		return tiles;
	}
	
	public ArrayList<SharkDecorator> getSharks() {
		return sharks;
	}

	public ArrayList<EagleDecorator> getEagles() {
		return eagles;
	}
	
	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}
	
	public boolean isContaingEagle(String eagleName) {
		for(int i=0; i< eagles.size(); i++)
			if(eagles.get(i).getName().compareToIgnoreCase(eagleName) == 0) {
				return true;
			}
		return false;
	}

	public boolean isContaingShark(String sharkName) {
		for(int i=0; i< sharks.size(); i++)
			if(sharks.get(i).getName().compareToIgnoreCase(sharkName) == 0) {
				return true;
			}
		return false;
	}
	
	public String[] eagles() {
		String[] items = new String[eagles.size()];
		for(int i=0; i< eagles.size(); i++)
			items[i] = eagles.get(i).getName();
		return items;
	}

	public String[] sharks() {
		String[] items = new String[sharks.size()];
		for(int i=0; i< sharks.size(); i++)
			items[i] = sharks.get(i).getName();
		return items;
	}
	
	public void setEnableDisableTiles(boolean enable) {
		for(Tile tile : tiles ) {
			tile.setEnabled(!enable);
		}
	}

	public Context getContext() {
		return context;
	}


	public Tile getAttacker() {
		return attacker;
	}


	public void setAttacker(Tile attacker) {
		this.attacker = attacker;
	}
}
