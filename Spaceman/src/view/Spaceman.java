package view;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import controller.LevelController;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;

public class Spaceman extends Character{

	public LevelController levelController;
	private static final int[] ROTATION_DEGREE = new int[] {0, 90, 180, 270};
	private int rotationIndex;
	public ImageView imageView;
	private int spawnX;
	private int spawnY;
	private double graphicalX;
	private double graphicalY;
	private int keyInput;
	private int currentRotation;
	
	private Image[] images;
	private Image[] shield;
	private Image[] imagesDefault;
	public boolean shieldStatus = false;
	
	private AudioClip pelletSound;
	private AudioClip warp;
	
	public Spaceman(LevelController levelController, int x, int y) {
	//public Spaceman(LevelController levelController, int x, int y) {
		keyInput = -1;
		
		//this.view = view;
		this.levelController = levelController;

		// Intialise Spaceman grid position
		this.x = x;
		this.y = y;
		spawnX = x;
		spawnY = y;
		graphicalX = x*TILE_WIDTH + GRAPHICAL_X_OFFSET;
		graphicalY = y*TILE_HEIGHT + GRAPHICAL_Y_OFFSET;

		// Intialise Spaceman moving left
		dx = -1;
		dy = 0;
		
		shield = new Image[] {new Image(getClass().getResourceAsStream("res/spacepac2shield.png")), 
				new Image(getClass().getResourceAsStream("res/spacepacshield.png")),
				new Image(getClass().getResourceAsStream("res/spacepac3shield.png")),
				new Image(getClass().getResourceAsStream("res/spacepacshield.png"))
		};
		
		Image startImage = new Image(getClass().getResourceAsStream("res/spacepac2.png")); 
		imagesDefault = new Image[] {
				startImage,
				new Image(getClass().getResourceAsStream("res/spacepac.png")),
				new Image(getClass().getResourceAsStream("res/spacepac3.png")),
				new Image(getClass().getResourceAsStream("res/spacepac.png"))
		};
		
		images = imagesDefault;
		
		imageIndex = 0;
		currentImage = images[imageIndex];
		rotationIndex = MOVE_LEFT;
		currentRotation = ROTATION_DEGREE[rotationIndex];

		imageView = new ImageView(startImage);
		imageView.setImage(images[imageIndex]);
		imageView.setX(graphicalX);
		imageView.setY(graphicalY);
		imageView.setRotate(currentRotation);

		getChildren().add(imageView);

		// remove later when movement logic is completed
		//status = MOVING;
		
		URL url = this.getClass().getResource("sound/boop.wav");
		pelletSound = new AudioClip(url.toString());
		
		url = this.getClass().getResource("sound/warp.wav");
		warp = new AudioClip(url.toString());

	}

	/*
	 * @see view.Character#moveOneStep()
	 */
	@Override
	public void moveOneStep() {
		levelController.respawnCollectables();
		if (imageIndex == 0 && this.isRunning()) {
			changeCurrentDirection(keyInput);
		}
		
		if (shieldStatus) {
			images = shield;
		} else {
			images = imagesDefault;
		}
		
		if (imageIndex < images.length-1) {
			imageIndex++;
			currentImage = images[imageIndex];
			imageView.setImage(currentImage);
		} else {
			imageIndex = 0;
			currentImage = images[imageIndex];
			imageView.setImage(currentImage);
		}
		
		if (this.isRunning()) {
			if (pelletSound.isPlaying()) {
				pelletSound.stop();
			}
			if (dx != 0 && dy == 0) {
				moveXAxis();
			} 
			if (dx == 0 && dy != 0) {
				moveYAxis();
			}
			
			imageView.setX(graphicalX);
			imageView.setY(graphicalY);
			imageView.setRotate(currentRotation);
			if (this.timeline.getStatus() == Status.RUNNING) {
				levelController.checkSpacemanAndAliens();
			}
		}
	}
	
	public void resetSpaceman() {
		stop();
		moveCounter = 0;
		
		keyInput = -1;
		
		// Intialise Spaceman grid position
		this.x = spawnX;
		this.y = spawnY;
		graphicalX = x*TILE_WIDTH + GRAPHICAL_X_OFFSET;
		graphicalY = y*TILE_HEIGHT + GRAPHICAL_Y_OFFSET;

		// Intialise Spaceman current direction
		this.dx = -1;
		this.dy = 0;
		
		imageIndex = 0;
		currentImage = images[imageIndex];
		rotationIndex = MOVE_LEFT;
		currentRotation = ROTATION_DEGREE[rotationIndex];

		imageView.setX(graphicalX);
		imageView.setY(graphicalY);
		imageView.setRotate(currentRotation);
		imageView.setImage(currentImage);
	}
	
	public void playPelletSound( ) {
		pelletSound.play();
	}
	
	private void moveXAxis() {
		int nextX = x + dx;
		// Wallcheck logic: If next destination is wall, do not move, else move as normal
		if (levelController.checkMap(nextX, y) == 1 || levelController.checkMap(nextX, y) == 9) {
			//pause animation here
			imageIndex=0;
		} else {
			//Move spaceman in the current direction by one step on the x-axis
			moveCounter++;
			if (moveCounter < ANIMATION_STEP) {
				graphicalX = graphicalX + (dx * MOVE_SPEED);
			} else {
				levelController.updateMap(dx,dy,x,y);
				
				moveCounter = 0;
				nextX = x + dx;
				// HARDCODED VALUES FOR TUNNEL X COORDINATE - USE GRID SIZE
				if (nextX < 1 && dx == -1 ) {
					if (levelController.getMode() == 4) {
						levelController.getCurrentView().stopAllChars();
						levelController.timeline.stop();
						levelController.changeMap(-1);					
					} else {
						x = 19;
					}
					warp.play();
				} else if (nextX > 19  && dx == 1 ) {
					if (levelController.getMode() == 4) {
						levelController.getCurrentView().stopAllChars();
						levelController.timeline.stop();
						levelController.changeMap(1);
					} else {
						x = 1;
					}
					warp.play();
				} else {
					x = x + dx;
				}
				graphicalX = x*TILE_WIDTH + GRAPHICAL_X_OFFSET;
			}
		}
	}

	private void moveYAxis() {
		int nextY = y + dy;
		if (levelController.checkMap(x,nextY) == 1 || levelController.checkMap(x, nextY) == 9) {
			imageIndex=0;
		} else {
			//Move spaceman in the current direction by one step on the x-axis
			moveCounter++;
			if (moveCounter < ANIMATION_STEP) {
				graphicalY = graphicalY + (dy * MOVE_SPEED);
			} else {
				levelController.updateMap(dx,dy,x,y);
				moveCounter = 0;
				y = y + dy;
				graphicalY = y*TILE_HEIGHT + GRAPHICAL_Y_OFFSET;
			}
		}
	}

	private void moveLeft() {
		// Prevent invalid direction changes
		int nextX = x - 1;
		if (levelController.checkMap(nextX, y) == 1 || levelController.checkMap(nextX, y) == 9) {
			return;
		}
		
		// Change direction
		dx = -1;
		dy = 0;
		rotationIndex = MOVE_LEFT;
		currentRotation = ROTATION_DEGREE[rotationIndex];
	}

	private void moveRight() {
		// Prevent invalid direction changes
		int nextX = x + 1;
		if (levelController.checkMap(nextX, y) == 1 || levelController.checkMap(nextX, y) == 9) {
			return;
		}
		// Change direction
		dx = 1;
		dy = 0;
		rotationIndex = MOVE_RIGHT;
		currentRotation = ROTATION_DEGREE[rotationIndex];
	}

	private void moveUp() {
		// Prevent invalid direction changes
		int nextY = y - 1;
		if (levelController.checkMap(x,nextY) == 1 || levelController.checkMap(x,nextY) == 9) {
			return;
		}
		// Change direction
		dx = 0;
		dy = -1;
		rotationIndex = MOVE_UP;
		currentRotation = ROTATION_DEGREE[rotationIndex];
	}

	private void moveDown() {
		// Prevent invalid direction changes
		int nextY = y + 1;
		if (levelController.checkMap(x,nextY) == 1 || levelController.checkMap(x,nextY) == 9) {
			return;
		}
		// Change direction
		dx = 0;
		dy = 1;
		rotationIndex = MOVE_DOWN;
		currentRotation = ROTATION_DEGREE[rotationIndex];
	}

	public void setKeyInput(int keyInput) {
		this.keyInput  = keyInput;
	}

	private void changeCurrentDirection(int keyInput) {
		if (keyInput == MOVE_LEFT) {
			moveLeft();
		} else if (keyInput == MOVE_RIGHT) {
			moveRight();
		} else if (keyInput == MOVE_UP) {
			moveUp();
		} else if (keyInput == MOVE_DOWN) {
			moveDown();
		}
	}
	
	public double getGraphicalX() {
		return graphicalX;
	}
	
	public double getGraphicalY() {
		return graphicalY;
	}
	
	public void updateShieldStatus() {
		if (!shieldStatus) {
			shieldStatus = true;
		}
	}
	
	
	public void setNewPosition(int direction) {
		for (int row = 0; row < 21; row++) {
			for (int col = 0; col < 21; col++) {
				if (levelController.getLevel().getCurrentMap().getData(row, col) == 5) {
					if (direction > 0) {
						x = col+1;
						y = row;
						dx = 1;		
					}
				} else if (levelController.getLevel().getCurrentMap().getData(row, col) == 6) {
					if (direction < 0) {
						x = col-1;
						y = row;
						dx = -1;
					}
				}

			}
		}
		graphicalX = x*TILE_WIDTH + GRAPHICAL_X_OFFSET;
		graphicalY = y*TILE_HEIGHT + GRAPHICAL_Y_OFFSET;
		imageView.setX(graphicalX);
		imageView.setY(graphicalY);	
	
	}
}
