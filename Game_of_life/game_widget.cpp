#include "game_widget.h"

game_widget::game_widget(QWidget *parent)
    : QWidget(parent), timer(new QTimer(this)), w_width(game.get_min_size().first),  w_height(game.get_min_size().second),
      w_changed(false),w_rule_b("3"),w_rule_s("23")
{
    timer->setInterval(50);
    repaint();
    connect(timer, SIGNAL(timeout()), this, SLOT(new_iteration()));
}

int game_widget::interval()
{
    return timer->interval();
}

void game_widget::set_interval(int msec)
{
    timer->setInterval(msec);
}

void game_widget::start()
{

    //std::cout << "Started!" << std::endl;
    timer->start();
    repaint();
}

void game_widget::new_iteration()
{
    game.new_iteration();
    //std::cout << "Iteration!" << std::endl;
    update();
}

void game_widget::stop()
{
    //std::cout << "Stop!" << std::endl;
    timer->stop();
}

void game_widget::clear()
{
    game.third_impact();
    //std::cout << "It all returns to nothing!" << std::endl;
    update();
}

void game_widget::paintEvent(QPaintEvent *)
{
    QPainter p(this);
    paint_grid(p);
    paint_field(p);
}

void game_widget::paint_grid(QPainter & p)
{
    QColor gridColor = Qt::white;
    gridColor.setAlpha(900);
    QRect borders(0, 0, width() - 1, height() - 1);
    gridColor.setAlpha(10);
    p.setPen(gridColor);

    int cellWidth = static_cast<double>(width()) / game.get_size().first + 0.5;
    for(int k = cellWidth; k < width(); k += cellWidth)
        p.drawLine(k, 0, k, height());

    int cellHeight = static_cast<double>(height()) / game.get_size().second + 0.5;
    for(int k = cellHeight; k < height(); k += cellHeight)
        p.drawLine(0, k, width(), k);

    p.drawRect(borders);
}

void game_widget::paint_field(QPainter & p)
{
    size_t x = 0u, y = 0u;
    double cellWidth = static_cast<double>(width()) / game.get_size().first;
    double cellHeight = static_cast<double>(height()) / game.get_size().second;

    for(y = 0u; y < game.get_size().second; y++) {
        for(x = 0u; x < game.get_size().first; x++) {
            if(game.get_field()[y * game.get_size().first + x] == 1) {
                qreal left = static_cast<qreal>(cellWidth * x);
                qreal top  = static_cast<qreal>(cellHeight * y);

                QRectF r(left, top, static_cast<qreal>(cellWidth),
                                    static_cast<qreal>(cellHeight));

                p.fillRect(r, QBrush(Qt::white));
            }
        }
    }
}

void game_widget::mousePressEvent(QMouseEvent *e)
{
    emit environment_changed(true);
    double cellWidth = static_cast<double>(width()) / game.get_size().first;
    double cellHeight = static_cast<double>(height()) / game.get_size().second;
    size_t y = static_cast<size_t>(e->y() / cellHeight);
    size_t x = static_cast<size_t>(e->x() / cellWidth);

    game.reverse_cell(x, y);
    update();
}

void game_widget::mouseMoveEvent(QMouseEvent * e)
{
    if (e->y() >= height() || e->y() < 0)
        return;
    if (e->x() >= width() || e->x() < 0)
        return;

    double cellWidth = static_cast<double>(width()) / game.get_size().first;
    double cellHeight = static_cast<double>(height()) / game.get_size().second;
    size_t y = static_cast<size_t>(e->y() / cellHeight);
    size_t x = static_cast<size_t>(e->x() / cellWidth);
    if(game.get_field()[y * game.get_size().first + x] != 1){
        game.set_alive(x, y);
        update();
    }
}

size_t game_widget::max_height()
{
    return game.get_max_size().second;
}

size_t game_widget::min_height()
{
    return game.get_min_size().second;
}

size_t game_widget::max_width()
{
    return game.get_max_size().first;
}

size_t game_widget::min_width()
{
    return game.get_min_size().first;
}

void game_widget::set_rule_b(QString B){
    w_changed = true;
    w_rule_b = B;
}

void game_widget::set_rule_s(QString S){
    w_changed = true;
    w_rule_s = S;
}

size_t game_widget::field_height()
{
    return game.get_size().first;
}

size_t game_widget::field_width()
{
    return game.get_size().second;
}

void game_widget::set_field_height(QString height)
{
    size_t u_height = height.toUInt();
    w_changed = true;
    w_height = u_height;
    emit height_changed(u_height);
}

void game_widget::set_field_width(QString width)
{
    size_t u_width = width.toUInt();
    w_changed = true;
    w_width = u_width;
    emit width_changed(u_width);
}

void game_widget::set_parametrs()
{
   if(w_changed) {
        game.set_w(w_width);
        game.set_h(w_height);
        game.set_rule_b(w_rule_b);
        game.set_rule_s(w_rule_s);
        w_changed = false;
        game.third_impact();
        update();
    }
}
