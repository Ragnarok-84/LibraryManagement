package ui;


import java.awt.*;


public final class UiStyles {
    private UiStyles() {}


    // Colors (approx to your mockup)
    public static final Color BG = new Color(248, 250, 255);
    public static final Color SIDEBAR_BG = new Color(245, 247, 255);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color PRIMARY = new Color(99, 102, 241); // indigo-500
    public static final Color INFO = new Color(59, 130, 246);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color DANGER = new Color(239, 68, 68);


    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);


    public static Panel card(int width, int height) {
        Panel p = new Panel();
        p.setBackground(CARD_BG);
        p.setPreferredSize(new Dimension(width, height));
        p.setLayout(new BorderLayout());
        p.setFont(BODY);
        p.setForeground(Color.DARK_GRAY);
        p.setName("card");
        return p;
    }
}