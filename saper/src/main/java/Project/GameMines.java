package Project;
import Project.*;
import static Project.Menu.Mines;
import static Project.Menu.Size;
import java.awt.*; //рисование окна
import java.awt.event.*;//обработка событий (нажатие мыши)
import javax.swing.*;//для граффики
import java.util.*;
import java.util.Timer;//библиотека с таймером

class GameMines extends JFrame{
    final String TITLE_OF_PROGRAM = "Сапёр";//название программы
    final String SIGN_OF_FLAG = "F";//значок флажка
    final int BLOCK_SIZE = 30; // размер одного блока
    int FIELD_SIZE = Size; // размер поля (в блоках)
    final int FIELD_DX = 16; // переменные FIELD_DX FIELD_DY определяются методом тыка, они нужны для того, чтобы клетки не уходили за рамки (зависит от разрешения)
    final int FIELD_DY = 55; 
    final int MOUSE_BUTTON_LEFT = 1;// для обработки мыши
    final int MOUSE_BUTTON_RIGHT = 3;
    int NUMBER_OF_MINES = Mines;//количество мин
    final int[] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0};//массив цветов
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];//двумерный массив для клеток
    Random random = new Random();//объект для заполнения бомб
    int countOpenedCells;//переменная для хранения открытых ячеек
    boolean youWon, bangMine; // переменные для победы или поражения
    int bangX, bangY; // переменная для координат взрыва

  
    GameMines() { //конструктор
        setTitle(TITLE_OF_PROGRAM); //создание заголовка окна
        setDefaultCloseOperation(EXIT_ON_CLOSE); //закрытие окна и программы путём нажатия на Х (кнопку закрытия)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-FIELD_SIZE*FIELD_SIZE)/2, (screenSize.height-FIELD_SIZE*FIELD_SIZE)/2, FIELD_SIZE * BLOCK_SIZE + FIELD_DX, FIELD_SIZE * BLOCK_SIZE + FIELD_DY);//метод создания окна
        setResizable(true);//изменение размерности окна, оставил true, чтобы если клетки выходят за рамки, можно было исправить
        final TimerLabel timeLabel = new TimerLabel(); //метка с таймером
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER); //выравнивание таймера по горизонтали
        final Canvas canvas = new Canvas();// элемент панели
        canvas.setBackground(Color.white); //цвет фона панели
        canvas.addMouseListener(new MouseAdapter() { //считывание нажатия на мышь 
            @Override //переопределение метода mouseReleased
            public void mouseReleased(MouseEvent e) { 
                super.mouseReleased(e); //вызов метода
                int x = e.getX()/BLOCK_SIZE; //получаем координаты клика мышки и преобразуем их в координаты ячейки,по которой кликнули
                int y = e.getY()/BLOCK_SIZE;
                if (!bangMine && !youWon) { //условие того, что игра ещё не окончена
                    if (e.getButton() == MOUSE_BUTTON_LEFT) // проверка нажатия левой кнопки мыши
                        if (field[y][x].isNotOpen()) { //
                            openCells(x, y);//открытие клетки
                            youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE - NUMBER_OF_MINES; // проверка победы
                            if (bangMine) { //проверка подрыва мины
                                bangX = x;
                                bangY = y;
                            }
                    }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag(); // проверка нажания правой кнопки мыши
                    if (bangMine||youWon) timeLabel.stopTimer(); // остановка таймера при окончании игры
                    canvas.repaint();//перерисовка игровой панели
                }
            }
        });
        add(BorderLayout.CENTER, canvas); //постановка игровой панели в центр экрана
        add(BorderLayout.SOUTH, timeLabel);// постановка таймера на игровой панели
        setVisible(true); //видимость поля
        initField();
    }

    void openCells(int x, int y) { // рекурсивный метод для открытия клеток
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return;
        //выход координат за границы поля вызывает выход из метода
        if (!field[y][x].isNotOpen()) return; // клетка уже открыта вызывает выход из метода
        field[y][x].open(); // после двух проверок выше ячейка открывается
        if (field[y][x].getCountBomb() > 0 || bangMine) return; //не пустая ячейка вызывает выход из метода
        for (int dx = -1; dx < 2; dx++)//цикл проходит по соседним 8 ячейкам и снова вызывает метод
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    void initField() { // метод иницилизации игрового поля
        int x, y, countMines = 0;  
// создание клеток поля
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell();
//создание минного поля
        while (countMines < NUMBER_OF_MINES) {
            //выбор случайных координат
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
             //если ячейка пуста ставится мина   
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        
        }
        
//считывание мин поблизости
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++) {                    
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    field[y][x].setCountBomb(count);
                }
    }

    class Cell { // класс клетки на игровом поле
        private int countBombNear; //кол-во бомб поблизости
        private boolean isOpen, isMine, isFlag; //переменные для отображения открытых ячеек, мин и флажков

        void open() { //открытие ячейки
            isOpen = true; //флаг открытия ячейки
            bangMine = isMine; //переменная взорванной мины
            if (!isMine) countOpenedCells++; //условие для незаминированной ячейки и увеличение счётчика открытых ячеек
        }

        void mine() { isMine = true; } //метод для минирования ячеек

        void setCountBomb(int count) { countBombNear = count; } //метод установки бомб

        int getCountBomb() { return countBombNear; } //метод установки на поле кол-ва бомб поблизости

        boolean isNotOpen() { return !isOpen; } // проверка на то закрыта или открыта ячейка

        boolean isMined() { return isMine; } // проверка на то заминирована ли ячейка

        void inverseFlag() { isFlag = !isFlag; } //инвертирование флажка

        void paintBomb(Graphics g, int x, int y, Color color) { 
            g.setColor(color); 
            g.fillRect(x*BLOCK_SIZE + 7, y*BLOCK_SIZE + 10, 18, 10);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x*BLOCK_SIZE + 9, y*BLOCK_SIZE + 8, 14, 14);
            g.setColor(Color.white);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 10, 4, 4);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) { //метод рисования строки
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
            g.drawString(str, x*BLOCK_SIZE + 8, y*BLOCK_SIZE + 26);
        }

        void paint(Graphics g, int x, int y) { //метод рисования игровых элементов
            g.setColor(Color.lightGray);
            g.drawRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            if (!isOpen) { //условие закрытой ячейки
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black); 
                //условие победы или поражения для отображения всех бомб на карте
                else {// иначе рисуется открытая ячейка
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red); //или рисуется флажок 
                }
            } else //условие открытой ячейки
                if (isMine) paintBomb(g, x, y, bangMine? Color.red : Color.black);  
                 //при наличии бомбы рисуется взорванная бомба
                else
                    if (countBombNear > 0) //при отсутсвии бомбы рисуется кол-во бомб по соседству 
                        paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }
    }

    class TimerLabel extends JLabel { //класс с таймером
        Timer timer = new Timer();

        TimerLabel() { timer.scheduleAtFixedRate(timerTask, 0, 1000); } 

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() { timer.cancel(); } //метод остановки таймера
    }

    class Canvas extends JPanel { //класс панели
        @Override //переопределение метода paint
        public void paint(Graphics g) {
            super.paint(g); //вызов метода
            for (int x = 0; x < FIELD_SIZE; x++) // отрисовка игровых ячеек
                for (int y = 0; y < FIELD_SIZE; y++) 
                    field[y][x].paint(g, x, y);
        }
    }
}