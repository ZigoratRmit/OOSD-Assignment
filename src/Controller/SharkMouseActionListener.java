package Controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JOptionPane;

import Entity.Shark;
import Model.Model;
import Patterns.Chain.AbstractLogger;
import Patterns.State.Context.GameStatus;
import View.Board;
import View.Tile;
import View.View;

public class SharkMouseActionListener  implements MouseListener 
{
	private Board board;
	private View view;
	private Model model;
	
	public SharkMouseActionListener(View view, Model model) {
		this.board = view.getBoard();
		this.model = model;
		this.view = view;
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{	
		// TODO Auto-generated method stub	
		if(model.getContext().getGameState() != GameStatus.SHARK)
		{
			JOptionPane.showMessageDialog(null,"It is Eagle turn");
			Tile tileItem = (Tile) e.getSource();
			if(tileItem != null && checkMovement(tileItem.getRow()-view.getBoard().getSelectedRow(),
					tileItem.getColumn()-view.getBoard().getSelectedColumn()))
			{
				for(Shark shark : model.getSharks())
				{
					if(shark.getName().contains(tileItem.getAttribute()))
					{
						if(shark.getLife() > 0)
						{
							shark.reduceLife(1);
							if(shark.getLife() <= 0)
							{
								doMovement(tileItem);
							}
						}
					}
				}
			}
			else
			{
				model.getLoggerChain().setwMessage(AbstractLogger.SHARK, "Wrong selection");
				view.updateEagleLog(model.getLoggerChain().message);
			}
		}
		else 
		{
			Tile tile = (Tile) e.getSource();
			if(tile != null) 
			{
					if(Arrays.asList(model.sharks()).contains(tile.getAttribute()))
					{
						if(board.getSelectedRow() == -1 && board.getSelectedColumn() == -1) 
						{
							board.setSelectedRow(tile.getRow());
							board.setSelectedColumn(tile.getColumn());
							board.setSelectedname(tile.getName());
							//set Shark message
							model.getLoggerChain().setwMessage(AbstractLogger.SHARK, "SHARK ( " +tile.getRow() + "," +tile.getColumn() + " )");
						}
//						view.getCurrentAnimalPanel().removeAll();
//						JLabel currentLabel = new JLabel("This is the animal that you choose");
//						JLabel sharkName = new JLabel(shark.getName());
//						JLabel sharkLife = new JLabel("Life: " + String.valueOf(shark.getLife()));
//						JLabel movementType = new JLabel("Movement: in '+' shape");
//						view.getCurrentAnimalPanel().add(currentLabel);
//						view.getCurrentAnimalPanel().add(sharkName);
//						view.getCurrentAnimalPanel().add(sharkLife);
//						view.getCurrentAnimalPanel().add(movementType);
//						view.getCurrentAnimalPanel().validate();
					}
			}
			view.updateSharkLog(model.getLoggerChain().message);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		Tile tile = (Tile) e.getSource();
 		if(tile != null) {

 		}	
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void showBoard() {
		EgaleMouseActionListener egaleMouseActionListener = new EgaleMouseActionListener(view,model);//.eagles());
		SharkMouseActionListener sharkMouseActionListener = new SharkMouseActionListener(view,model);//.sharks());
		PieceMoveActionListener pieceMoveActionListener = new PieceMoveActionListener(view,model);

		int item = 0;
		Tile tile;
		for(int x = 0;x<view.getBoard().getRow();x++)
			for(int y = 0;y<view.getBoard().getColumn();y++) 
			{
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

	public void doMovement(Tile tileItem) {
		if(view.getBoard().getSelectedRow() != -1 && view.getBoard().getSelectedColumn() != -1) {
			//calculate distance
			double x = tileItem.getRow() - view.getBoard().getSelectedRow();
			double y = tileItem.getColumn() - view.getBoard().getSelectedColumn();

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
				if(destinationAttribute.equalsIgnoreCase("Black") || 
						destinationAttribute.equalsIgnoreCase("Bateleur") || 
						destinationAttribute.equalsIgnoreCase("Bald"))
				{
					sourceAttributeChange = "island";
				}
				else
				{
					sourceAttributeChange = "ocean";
				}

				if(destinationTile.getCurrentTileAttribute() == null)
				{
					if(destinationAttribute.equalsIgnoreCase("Black") || 
							destinationAttribute.equalsIgnoreCase("Bateleur") || 
							destinationAttribute.equalsIgnoreCase("Bald"))
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

				if(sourceAttribute.equalsIgnoreCase("ocean") && (destinationAttribute.equalsIgnoreCase("Black") || 
						destinationAttribute.equalsIgnoreCase("Bateleur") || 
						destinationAttribute.equalsIgnoreCase("Bald")))
				{
					destinationTile.setCurrentTileAttribute("EagleOcean");
				}
				else if(sourceAttribute.equalsIgnoreCase("island") && (destinationAttribute.equalsIgnoreCase("Black") || 
						destinationAttribute.equalsIgnoreCase("Bateleur") || 
						destinationAttribute.equalsIgnoreCase("Bald")))
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
				showBoard();
				view.getBoard().validate();
				if(model.getContext().getGameState() == GameStatus.SHARK) {
					//Shark log show in right side of panel
					model.getLoggerChain().setwMessage(AbstractLogger.SHARK, destinationAttribute + " Moved ( " +x1+ "," +y1 + " )");
					view.updateSharkLog(model.getLoggerChain().message);
					//scored the Shark
					view.UpdateScore(model.getContext().getGameState() == GameStatus.EGALE,1);
					model.getLoggerChain().setwMessage(AbstractLogger.SHARK, "Scoreed");
					view.updateSharkLog(model.getLoggerChain().message);
					//Change state to Eagle turn
					model.getContext().setGameState(GameStatus.EGALE);
				}
				else {
					//Eagle log show in right side of panel
					model.getLoggerChain().setwMessage(AbstractLogger.EAGLE, destinationAttribute + " Moved ( " +x1+ "," +y1 + " )");
					view.updateEagleLog(model.getLoggerChain().message);
					//scored the Eagle
					view.UpdateScore(model.getContext().getGameState() == GameStatus.EGALE,1);
					model.getLoggerChain().setwMessage(AbstractLogger.EAGLE, "Scoreed");
					view.updateEagleLog(model.getLoggerChain().message);
					//Change state to Shark turn
					model.getContext().setGameState(GameStatus.SHARK);
				}
				view.changeGameStateTimer(model.getContext().getGameState());
			}
			else
			{
				if(model.getContext().getGameState() == GameStatus.EGALE) {
					model.getLoggerChain().setwMessage(AbstractLogger.EAGLE, "Shark movement is wrong");
					view.updateEagleLog(model.getLoggerChain().message);
					JOptionPane.showMessageDialog(null,"Egale movement is wrong");
				}
				else if(model.getContext().getGameState() == GameStatus.SHARK) {
					model.getLoggerChain().setwMessage(AbstractLogger.SHARK, "Shark movement is wrong");
					view.updateSharkLog(model.getLoggerChain().message);
					JOptionPane.showMessageDialog(null,"Shark movement is wrong");
				}
			}
			view.getBoard().setSelectedRow(-1);
			view.getBoard().setSelectedColumn(-1);			
		}		
	}

	public boolean checkMovement(double x, double y) {
		boolean result = false;
		if(model.getContext().getGameState() == GameStatus.EGALE) {// view.getBoard().getEagleSharkTurn()) {// EagleOrShark) {
			//find the eagle name for selecting different movement.
			if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(0).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(1).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
			else if(view.getBoard().getSelectedname().compareToIgnoreCase(model.getEagles().get(2).getName()) == 0)
				result = ((Math.abs(x) + Math.abs(y)) <= 3);
		}
		else if(model.getContext().getGameState() == GameStatus.SHARK){
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
}
