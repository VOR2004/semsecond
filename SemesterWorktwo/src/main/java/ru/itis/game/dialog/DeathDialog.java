package ru.itis.game.dialog;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import ru.itis.res.Colors;
public class DeathDialog {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 300;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_SPACING = 20;
    private final ActionListener mainMenuAction;
    private final ActionListener restartAction;
    private Rectangle2D mainMenuButtonBounds;
    private  Rectangle2D restartButtonBounds;
    private boolean isClickMainMenu = false;
    private boolean isClickRestart = false;
    private boolean isShowed = false;
    public DeathDialog(int x, int y, ActionListener mainMenuAction, ActionListener restartAction) {
        this.mainMenuAction = mainMenuAction;
        this.restartAction = restartAction;
    }
    public void draw(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        int panelX = x + (width - DIALOG_WIDTH) / 2;
        int panelY = y + (height - DIALOG_HEIGHT) / 2;
        g2d.setColor(Colors.DIALOG_BACKGROUND);
        g2d.fillRect(panelX, panelY, DIALOG_WIDTH, DIALOG_HEIGHT);

        g2d.setColor(Colors.TEXT_COLOR);
        Font font = new Font("Arial", Font.BOLD, 30);
        g2d.setFont(font);

        String text = "Вы погибли!";
        FontMetrics fm = g2d.getFontMetrics(font);
        int textX = panelX + (DIALOG_WIDTH - fm.stringWidth(text)) / 2;
        int textY = panelY + fm.getHeight() + 20;
        g2d.drawString(text, textX, textY);


        int mainMenuButtonX = panelX + (DIALOG_WIDTH - BUTTON_WIDTH * 2 - BUTTON_SPACING) / 2;
        int mainMenuButtonY = panelY + DIALOG_HEIGHT - BUTTON_HEIGHT - 40;
        int restartButtonX = mainMenuButtonX + BUTTON_WIDTH + BUTTON_SPACING;
        mainMenuButtonBounds = new Rectangle2D.Double(mainMenuButtonX, mainMenuButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        restartButtonBounds = new Rectangle2D.Double(restartButtonX, mainMenuButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        Color mainMenuColor = isClickMainMenu ? Colors.BUTTON_COLOR.brighter() : Colors.BUTTON_COLOR;
        Color restartColor = isClickRestart ? Colors.BUTTON_COLOR.brighter() : Colors.BUTTON_COLOR;
        g2d.setColor(mainMenuColor);
        g2d.fillRect(mainMenuButtonX, mainMenuButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        g2d.setColor(restartColor);
        g2d.fillRect(restartButtonX, mainMenuButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);

        g2d.setColor(Colors.TEXT_COLOR);
        Font buttonFont = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(buttonFont);

        String mainMenuText = "Главное меню";
        FontMetrics buttonFm = g2d.getFontMetrics(buttonFont);
        int mainMenuTextX = mainMenuButtonX + (BUTTON_WIDTH - buttonFm.stringWidth(mainMenuText)) / 2;
        int mainMenuTextY = mainMenuButtonY + buttonFm.getHeight() + 2;
        g2d.drawString(mainMenuText, mainMenuTextX, mainMenuTextY);

        String restartText = "Заново";
        int restartTextX = restartButtonX + (BUTTON_WIDTH - buttonFm.stringWidth(restartText)) / 2;
        int restartTextY = mainMenuButtonY + buttonFm.getHeight() + 2;
        g2d.drawString(restartText, restartTextX, restartTextY);
    }
    public boolean checkButtonClick(int mouseX, int mouseY){
        if (mainMenuButtonBounds.contains(mouseX, mouseY)){
            mainMenuAction.actionPerformed(null);
            return true;
        } else if(restartButtonBounds.contains(mouseX, mouseY)){
            restartAction.actionPerformed(null);
            return true;
        }
        return false;
    }
    public void setClickMainMenu(boolean clickMainMenu) {
        isClickMainMenu = clickMainMenu;
    }

    public void setClickRestart(boolean clickRestart) {
        isClickRestart = clickRestart;
    }
    public void show(){
        isShowed = true;
    }
    public void hide(){
        isShowed = false;
    }
    public boolean isShowed() {
        return isShowed;
    }
}