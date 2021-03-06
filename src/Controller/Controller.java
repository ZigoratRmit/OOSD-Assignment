package Controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import Model.Model;
import Patterns.Chain.AbstractGameLogger;
import Patterns.Command.CommandLineChanger;
import Patterns.State.Context.GameStatus;
import View.*;

public class Controller {

	private View view;	
	private Model model;

	public Controller() {

	}


	public void initView() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					view.getFrame().setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void updateBoard() {
		int item = 0;
		Tile tile;
		for(int x = 0;x<view.getBoard().getRow();x++)
			for(int y = 0;y<view.getBoard().getColumn();y++) {
				tile = model.getTiles().get(item++);
				view.getBoard().add(tile);
			}		
	}
	
	public void showBoard() {
		EgaleMouseActionListener egaleMouseActionListener = new EgaleMouseActionListener(this);
		SharkMouseActionListener sharkMouseActionListener = new SharkMouseActionListener(this);
		PieceMoveActionListener pieceMoveActionListener = new PieceMoveActionListener(this);

		int item = 0;
		Tile tile;
		for(int x = 0;x<view.getBoard().getRow();x++)
			for(int y = 0;y<view.getBoard().getColumn();y++) {
				tile = model.getTiles().get(item++);
				String attribute = tile.getAttribute();
				if(model.isContaingEagle(attribute)) 
				{
					tile.addMouseListener(egaleMouseActionListener);
				}
				else if(model.isContaingShark(attribute))
				{
					tile.addMouseListener(sharkMouseActionListener);
				}
				else
				{
					tile.addMouseListener(pieceMoveActionListener);
				}
				view.getBoard().add(tile);
			}		
	}

	private void createTimer() {
		ActionListener countDown=new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double timerCounter = view.updateTimer();
				if(timerCounter < 0) {
					if(model.getContext().getGameState() == GameStatus.EGALE)
						model.getGameState().doSharkAction(model.getContext());
					else
						model.getGameState().doEgaleAction(model.getContext());
					view.changeGameStateTimer(model.getContext().getGameState());
				}
			}
		};
		view.setTimer( new Timer(100 ,countDown));
		view.getTimer().start();
		view.UpdateTurnViewStatus(model.getContext().getGameState());
	}

	private void initMouseListener() {
		view.getMnuStartStop().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				startStopClick();
			}	
		});		

		view.getMnuExit().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doExit();
			}	
		});

		view.getMnuBoardOptions().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Border options event to change the game border size
				boardOptionsClick(); 	
			}
		});

		view.getMnuSave().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//save event for saving the current game status
				doSave();
			}
		});

		view.getMnuNew().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doOjectsLists();
			}
		});		

		view.getBtnUndoButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//UNDO Events
				if(model.getUndoRedoManager().canUndo())
					model.getUndoRedoManager().undo();
				else
					JOptionPane.showMessageDialog(null,"End of UNDO");
			}			
		});

		view.getBtnRedoButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//REDO Events
				if(model.getUndoRedoManager().canRedo())
					model.getUndoRedoManager().redo();
				else
					JOptionPane.showMessageDialog(null,"End of REDO");
			}			
		});
	}

	public void initController() {					
		model.getApplicationConfiguration().ReadApplicationConfiguration();
		model.checkLoadingGame();

		view.getBoard().setRow(model.getApplicationConfiguration().getBoardRows());
		view.getBoard().setColumn(model.getApplicationConfiguration().getBoardColumns());
		view.getBoard().initBoard();		

		model.initModel(view.getBoard().getRow(),view.getBoard().getColumn());	

		initView();
		showBoard();

		view.setLblEgaleScore(model.getApplicationConfiguration().getEgaleScore());
		view.setLblSharkScore(model.getApplicationConfiguration().getSharkScore());
		view.showGameDetails(model.eagles(),model.sharks());
		if(model.getContext().getGameState() == GameStatus.START)
			model.getGameState().doEgaleAction(model.getContext());
		createTimer();
		initMouseListener();
	}

	private void doSave() {
		model.getApplicationConfiguration().WriteApplicationConfiguration(this);
		JOptionPane.showMessageDialog(null,"Current game was saved.");
	}

	private void doOjectsLists() {
		String s;
		s = "Egales : \n" + model.getEglesList() + "\n" + "Sharks : \n" + model.getSharksList();
		JOptionPane.showMessageDialog(null,s);
	}

	private void doExit() {
		int result = JOptionPane.showConfirmDialog(null, "Do you want to exit and save?");
		if(result == 0) {
			model.getApplicationConfiguration().WriteApplicationConfiguration(this);
			System.exit(0);
		}
		if(result == 1) {
			System.exit(0);
		}
	}

	private void boardOptionsClick() {
		try {
			BoardOptions dialog = new BoardOptions(view.getBoard().getRow(),view.getBoard().getColumn());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			if(view.getBoard().getRow() != dialog.getRows()) {
				view.getBoard().setRow(dialog.getRows());
				view.getBoard().setColumn(dialog.getColumns());
				view.getBoard().updateBoard();
				model.initModel(view.getBoard().getRow(),view.getBoard().getColumn());
				view.getBoard().removeAll();
				showBoard();
				view.getBoard().validate();
				JOptionPane.showMessageDialog(null,"Board was changed and game was rested.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void doMovement(Tile tileItem) {
		if(view.getBoard().getSelectedRow() != -1 && view.getBoard().getSelectedColumn() != -1) {
			//calculate distance
			double x = tileItem.getRow() - view.getBoard().getSelectedRow();
			double y = tileItem.getColumn() - view.getBoard().getSelectedColumn();
			boolean islandCheck = true;
			int tileX = view.getBoard().getSelectedRow() - 1;
			int tileY = view.getBoard().getSelectedColumn() - 1;
			int tileLocation = tileX * 8 + tileY;
			Tile originalTile = model.getTiles().get(tileLocation);
			if(model.getContext().getGameState() == GameStatus.SHARKATTACK && 
					!originalTile.getAttribute().equalsIgnoreCase("blue shark"))
			{
				islandCheck = this.checkIslandForShark(tileItem);
			}
			if(islandCheck)
			{
				boolean isMoveAllowed = checkMovement(x,y);
				if(isMoveAllowed) {				
					//find source and destination location in the board
					int x1 = tileItem.getRow();   
					int y1 = tileItem.getColumn();
					int x2 = view.getBoard().getSelectedRow() ;
					int y2 = view.getBoard().getSelectedColumn();

					int source = ((x1 - 1) * 8) + (y1 - 1);
					int destination = ((x2 - 1) * 8) + (y2 - 1);

					Tile sourceTile = model.getTiles().get(source);
					Tile destinationTile = model.getTiles().get(destination);
					String sourceAttribute = sourceTile.getAttribute();
					String destinationAttribute = destinationTile.getAttribute();

					String sourceAttributeChange = "";
					if(model.isContaingEagle(destinationAttribute))
					{
						sourceAttributeChange = "island";
					}
					else
					{
						sourceAttributeChange = "ocean";
					}

					if(destinationTile.getCurrentTileAttribute() == null)
					{
						if(model.isContaingEagle(destinationAttribute))
						{
							model.getTiles().get(destination).setCurrentTileAttribute("EagleIsland");
						}
						else
						{
							model.getTiles().get(destination).setCurrentTileAttribute("SharkOcean");
						}

						if(destinationTile.getCurrentTileAttribute().equalsIgnoreCase("EagleOcean")
								|| destinationTile.getCurrentTileAttribute().equalsIgnoreCase("SharkOcean"))
						{
							sourceAttributeChange = "ocean";
						}
						else
						{
							sourceAttributeChange = "island";
						}
					}
					else
					{
						if(destinationTile.getCurrentTileAttribute().equalsIgnoreCase("EagleIsland"))
						{
							sourceAttributeChange = "island";
						}
						else
						{
							sourceAttributeChange = "ocean";
						}
					}

					if(sourceAttribute.equalsIgnoreCase("ocean") && model.isContaingEagle(destinationAttribute))
					{
						destinationTile.setCurrentTileAttribute("EagleOcean");
					}
					else if(sourceAttribute.equalsIgnoreCase("island") && model.isContaingEagle(destinationAttribute))
					{
						destinationTile.setCurrentTileAttribute("EagleIsland");
					}


					model.getTiles().get(source).setRow(x2);
					model.getTiles().get(source).setColumn(y2);
					model.getTiles().get(destination).setRow(x1);
					model.getTiles().get(destination).setColumn(y1);
					Collections.swap(model.getTiles(), source, destination);

					model.getTiles().get(destination).setAttribute(sourceAttributeChange);
					model.setImageToTile(model.getTiles().get(destination), sourceAttributeChange);

					view.getBoard().removeAll();
					updateBoard();
					view.getBoard().validate();
					switch(model.getContext().getGameState()) {
					case SHARK:
					case SHARKATTACK:
						//Shark log show in right side of panel
						model.getLoggerChain().setwMessage(AbstractGameLogger.SHARK, destinationAttribute + " Moved ( " +x1+ "," +y1 + " )");
						view.updateSharkLog(model.getLoggerChain().message);
						//scored the Shark
						view.updateScore(false,1);
						model.getLoggerChain().setwMessage(AbstractGameLogger.SHARK, "Scoreed");
						view.updateSharkLog(model.getLoggerChain().message);
						//Change state to Eagle turn
						model.getGameState().doEgaleAction(model.getContext());
						
						//save command for doing UNDO and REDO
						model.getUndoRedoManager().addUndoRedoManager(
								new CommandLineChanger(
										this,
										source,
										destination));
						break;
					case EGALE:
					case EGALEATTACK:
						model.getLoggerChain().setwMessage(AbstractGameLogger.EAGLE, destinationAttribute + " Moved ( " +x1+ "," +y1 + " )");
						view.updateEagleLog(model.getLoggerChain().message);
						//scored the Eagle
						view.updateScore(true,1);
						model.getLoggerChain().setwMessage(AbstractGameLogger.EAGLE, "Scoreed");
						view.updateEagleLog(model.getLoggerChain().message);
						//Change state to Shark turn
						model.getGameState().doSharkAction(model.getContext());
						
						//save command for doing UNDO and REDO
						model.getUndoRedoManager().addUndoRedoManager(
								new CommandLineChanger(
										this,
										source,
										destination));
						break;
					default:
						break;
					}
					if((model.getContext().getGameState() == GameStatus.SHARK) || (model.getContext().getGameState() == GameStatus.EGALE))
						view.changeGameStateTimer(model.getContext().getGameState());
					view.changeGameStateTimer(model.getContext().getGameState());
				}
				else
				{
					if(model.getContext().getGameState() == GameStatus.EGALE) {
						model.getLoggerChain().setwMessage(AbstractGameLogger.EAGLE, "Shark movement is wrong");
						view.updateEagleLog(model.getLoggerChain().message);
					}
					else if(model.getContext().getGameState() == GameStatus.SHARK) {
						model.getLoggerChain().setwMessage(AbstractGameLogger.SHARK, "Shark movement is wrong");
						view.updateSharkLog(model.getLoggerChain().message);
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Shark can not go through the island");
			}
			view.getBoard().setSelectedRow(-1);
			view.getBoard().setSelectedColumn(-1);
		}		
	}
	
	public boolean checkMovement(double x, double y) {
		boolean result = false;
		if(model.getContext().getGameState() == GameStatus.EGALEATTACK) {
			//find the eagle name for selecting different movement.
			if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(0).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(1).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(2).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
		}
		else if(model.getContext().getGameState() == GameStatus.SHARKATTACK){
			//find the shark name for selecting different movement.
			if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getSharks().get(0).getName()) == 0)	
				result = (Math.abs(x) == 0 ) ||( Math.abs(y) == 0);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getSharks().get(1).getName()) == 0)	
				result = (Math.abs(x) == 0 ) ||( Math.abs(y) == 0);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getSharks().get(2).getName()) == 0)	
				result = (Math.abs(x) == 0 ) ||( Math.abs(y) == 0);
		}
		return result;
	}
	
	public boolean checkIslandForShark(Tile tileItem)
	{
		boolean returnValue = true;
		double x = tileItem.getRow() - view.getBoard().getSelectedRow();
		double y = tileItem.getColumn() - view.getBoard().getSelectedColumn();
		if((Math.abs(x) != 0 ) && ( Math.abs(y) != 0))
		{
			returnValue = false;
		}
		int x1 = tileItem.getRow();   
		int y1 = tileItem.getColumn();
		int x2 = view.getBoard().getSelectedRow();
		int y2 = view.getBoard().getSelectedColumn();
		if(model.getContext().getGameState() == GameStatus.SHARKATTACK)
		{
			if(x == 0 && y1 < y2)
			{
				for(int i = (y1 - 1);i<(y2-1);i++)
				{
					int tileCheck = ((x1-1)*8) + i;
					if(model.getTiles().get(tileCheck).getAttribute().equalsIgnoreCase("island"))
					{
						returnValue = false;
						break;
					}
				}
			}
			else if (x == 0 && y1 > y2)
			{
				for(int i = (y2 - 1);i<(y1-1);i++)
				{
					int tileCheck = ((x1-1)*8) + i;
					if(model.getTiles().get(tileCheck).getAttribute().equalsIgnoreCase("island"))
					{
						returnValue = false;
						break;
					}
				}
			}
			else if (y == 0 && x1 > x2)
			{
				for(int i = (x2 - 1);i<(x1-1);i++)
				{
					int tileCheck = (i*8) + (y1-1);
					if(model.getTiles().get(tileCheck).getAttribute().equalsIgnoreCase("island"))
					{
						returnValue = false;
						break;
					}
				}
			}
			else if (y == 0 && x1 < x2)
			{
				for(int i = (x1 - 1);i<(x2-1);i++)
				{
					int tileCheck = (i*8) + (y1-1);
					if(model.getTiles().get(tileCheck).getAttribute().equalsIgnoreCase("island"))
					{
						returnValue = false;
						break;
					}
				}
			}
		}
		return returnValue;
	}
	
	private void startStopClick() {
		boolean enable = false;
		switch(model.getContext().getGameState()) {
		case PAUSE:
			model.getGameState().doGameStartAction(model.getContext());
			enable = true;
			break;
		default:
			model.getGameState().doGamePauseAction(model.getContext());
			break;
		}
		view.startStopGame(enable);
		model.setEnableDisableTiles(enable);
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
}
