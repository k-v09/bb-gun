import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChessGame extends JFrame {
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;
    private ChessBoard board;
    private boolean isWhiteTurn = true;
    private boolean isDefaultTheme = true;
    private static final Color DEFAULT_LIGHT = new Color(240, 217, 181);
    private static final Color DEFAULT_DARK = new Color(181, 136, 99);
    private static final Color ALTERNATE_LIGHT = new Color(224, 237, 217); 
    private static final Color ALTERNATE_DARK = new Color(29, 13, 40);
    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"; 

    public ChessGame() {
        setTitle("Chess is a Game");
        setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JToggleButton themeToggle = new JToggleButton("Theme");
        themeToggle.addActionListener(e -> {
            isDefaultTheme = !isDefaultTheme;
            board.repaint();
        });
        controlPanel.add(themeToggle);
        add(controlPanel, BorderLayout.NORTH);

        
        board = new ChessBoard();
        add(board, BorderLayout.CENTER);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    class ChessBoard extends JPanel {
        private ChessPiece[][] pieces;
        private Point selectedPiece = null;

        public ChessBoard() {
            setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE));
            pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
            initializeFromFEN(INITIAL_FEN);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = e.getY() / TILE_SIZE;
                    int col = e.getX() / TILE_SIZE;
                    handleClick(row, col);
                }
            });
        }

        public void initializeFromFEN(String fen) {
            // Clear the board first
            pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
            
            // Split FEN string into its components
            String[] fenParts = fen.split(" ");
            String piecePositions = fenParts[0];
            
            // Parse piece positions
            String[] ranks = piecePositions.split("/");
            for (int rank = 0; rank < 8; rank++) {
                int file = 0;
                for (char c : ranks[rank].toCharArray()) {
                    if (Character.isDigit(c)) {
                        file += Character.getNumericValue(c);
                    } else {
                        boolean isWhite = Character.isUpperCase(c);
                        char pieceType = Character.toUpperCase(c);
                        pieces[rank][file] = new ChessPiece(pieceType, isWhite);
                        file++;
                    }
                }
            }
            
            // Parse active color
            isWhiteTurn = fenParts[1].equals("w");
            
            // Could also parse castling rights, en passant, halfmove clock, and fullmove number
            // from fenParts[2], fenParts[3], fenParts[4], and fenParts[5] respectively
        }

        private void handleClick(int row, int col) {
            if (selectedPiece == null) {
                if (pieces[row][col] != null && pieces[row][col].isWhite == isWhiteTurn) {
                    selectedPiece = new Point(row, col);
                    repaint();
                }
            } else {
                movePiece(selectedPiece.x, selectedPiece.y, row, col);
                selectedPiece = null;
                repaint();
            }
        }

        private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                pieces[toRow][toCol] = pieces[fromRow][fromCol];
                pieces[fromRow][fromCol] = null;
                isWhiteTurn = !isWhiteTurn;
                
                // Generate FEN string after move
                String currentFEN = generateFEN();
            }
        }

        private String generateFEN() {
            StringBuilder fen = new StringBuilder();
            for (int rank = 0; rank < 8; rank++) {
                int emptyCount = 0;
                for (int file = 0; file < 8; file++) {
                    ChessPiece piece = pieces[rank][file];
                    if (piece == null) {
                        emptyCount++;
                    } else {
                        if (emptyCount > 0) {
                            fen.append(emptyCount);
                            emptyCount = 0;
                        }
                        char pieceChar = piece.type;
                        fen.append(piece.isWhite ? pieceChar : Character.toLowerCase(pieceChar));
                    }
                }
                if (emptyCount > 0) {
                    fen.append(emptyCount);
                }
                if (rank < 7) fen.append('/');
            }
            fen.append(' ').append(isWhiteTurn ? 'w' : 'b');
            fen.append(" KQkq - 0 1");
            
            return fen.toString();
        }

        private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
            if (pieces[toRow][toCol] == null) return true;
            return pieces[toRow][toCol].isWhite != pieces[fromRow][fromCol].isWhite;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            drawPieces(g);
            if (selectedPiece != null) {
                highlightSquare(g, selectedPiece.x, selectedPiece.y);
            }
        }

        private void drawBoard(Graphics g) {
            Color lightColor = isDefaultTheme ? DEFAULT_LIGHT : ALTERNATE_LIGHT;
            Color darkColor = isDefaultTheme ? DEFAULT_DARK : ALTERNATE_DARK;

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if ((row + col) % 2 == 0) {
                        g.setColor(lightColor);
                    } else {
                        g.setColor(darkColor);
                    }
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        private void drawPieces(Graphics g) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (pieces[row][col] != null) {
                        drawPiece(g, row, col, pieces[row][col]);
                    }
                }
            }
        }

        private void drawPiece(Graphics g, int row, int col, ChessPiece piece) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String symbol = getPieceSymbol(piece.type);
            FontMetrics metrics = g2d.getFontMetrics();
            
            int x = col * TILE_SIZE + (TILE_SIZE - metrics.stringWidth(symbol)) / 2;
            int y = row * TILE_SIZE + ((TILE_SIZE + metrics.getHeight()) / 2) - 5;

            
            if (piece.isWhite) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawString(symbol, x, y);
            } else {
                g2d.setColor(isDefaultTheme ? Color.BLACK : new Color(40, 40, 40));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawString(symbol, x, y);
            }
        }

        private String getPieceSymbol(char type) {
            switch (type) {
                case 'K': return "♔";
                case 'Q': return "♕";
                case 'R': return "♖";
                case 'B': return "♗";
                case 'N': return "♘";
                case 'P': return "♙";
                default: return "";
            }
        }

        private void highlightSquare(Graphics g, int row, int col) {
            g.setColor(new Color(255, 255, 0, 100));
            g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    static class ChessPiece {
        char type;
        boolean isWhite;

        ChessPiece(char type, boolean isWhite) {
            this.type = type;
            this.isWhite = isWhite;
        }
    }
    static class Pawn extends ChessPiece {
        char type;
        boolean isWhite;

        Pawn(char type, boolean isWhite) {
            super('P', isWhite);
        }
    }
    static class Knight extends ChessPiece {
        char type;
        boolean isWhite;

        Knight(char type, boolean isWhite) {
            super('N', isWhite);
        }
    }
    static class Bishop extends ChessPiece {
        char type;
        boolean isWhite;

        Bishop(char type, boolean isWhite) {
            super('B', isWhite);
        }
    }
    static class Rook extends ChessPiece {
        char type;
        boolean isWhite;

        Rook(char type, boolean isWhite) {
            super('R', isWhite);
        }
    }
    static class Queen extends ChessPiece {
        char type;
        boolean isWhite;

        Queen(char type, boolean isWhite) {
            super('Q', isWhite);
        }
    }
    static class King extends ChessPiece {
        char type;
        boolean isWhite;

        King(char type, boolean isWhite) {
            super('K', isWhite);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }
}