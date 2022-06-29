#include "game_widget.h"

game_widget::game_widget(QWidget *parent)
    : QWidget(parent), timer(new QTimer(this)),
      w_changed(false),w_rule_b("3"),w_rule_s("23"), c_size(5), zoom(1)
{
    timer->setInterval(50);
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
    emit game_started(true);
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
    emit game_ended(true);
}

void game_widget::clear()
{
    game.third_impact();
    //std::cout << "It all returns to nothing!" << std::endl;
    update();
}

void game_widget::paintEvent(QPaintEvent *)
{
   size_t zoom_size = zoom * c_size;

   size_t left = 0;
   size_t right = std::min<size_t>(game.get_size().first, left + std::ceil(width() / 2) + 2);
   size_t up = 0;
   size_t down = std::min<size_t>(game.get_size().second, up + std::ceil(height() / 2) + 2);
   QPainter p(this);
   p.setPen(Qt::black);
   for(size_t i = up;i < down;++i){
       size_t y = i * zoom_size;
       for(size_t j = left;j < right;++j){
            size_t x = j * zoom_size;
            p.setBrush(Qt::white);
            if(game.get_field()[i * game.get_size().first + j] == 1){
                p.setBrush(Qt::black);
            }
            p.drawRect(x, y, zoom_size, zoom_size);
       }
   }
}

void game_widget::mousePressEvent(QMouseEvent *e)
{
    Qt::MouseButtons mb = e->buttons();
    size_t zoom_size = zoom * c_size;
    if(mb & Qt::LeftButton){
        size_t y = static_cast<size_t>(e->y() / zoom_size);
        size_t x = static_cast<size_t>(e->x() / zoom_size);
        if(x > game.get_size().first || y > game.get_size().second){
            return;
        }
        game.reverse_cell(x, y);
        update();
    }
}

void game_widget::mouseMoveEvent(QMouseEvent * e)
{
    if (e->y() >= height() || e->y() < 0)
        return;
    if (e->x() >= width() || e->x() < 0)
        return;
    Qt::MouseButtons mb = e->buttons();
    if(mb & Qt::LeftButton){
        size_t zoom_size = zoom * c_size;
        size_t y = static_cast<size_t>(e->y() / zoom_size);
        size_t x = static_cast<size_t>(e->x() / zoom_size);
        if(x > game.get_size().first || y > game.get_size().second){
            return;
        }
        game.set_alive(x, y);
        update();
    }
}

void game_widget::wheelEvent(QWheelEvent * e)
{
    size_t zoom_factor = 1;
    if(e->delta() > 0){
        if(zoom + zoom_factor <= 5){
            zoom += zoom_factor;
        }
    }
    else if(e->delta() < 0){
        if(zoom - zoom_factor > 0){
            zoom -= zoom_factor;
        }
    }
    update();
}

size_t game_widget::max_height()
{
    return game.get_max_size().second;
}

size_t game_widget::min_width()
{
    return game.get_min_size().first;
}

size_t game_widget::max_width()
{
    return game.get_max_size().first;
}

size_t game_widget::min_height()
{
    return game.get_min_size().second;
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
    size_t u_height = min_height();
    if(!height.isEmpty()){
        u_height = height.toUInt();
    }
    w_changed = true;
    w_height = u_height;
}

void game_widget::set_field_width(QString width)
{
    size_t u_width = min_width();
    if(!width.isEmpty()){
        u_width = width.toUInt();
    }
    w_changed = true;
    w_width = u_width;
}

void game_widget::apply()
{
   if(w_changed) {
        game.set_w(w_width);
        game.set_h(w_height);
        if(!w_rule_b.isEmpty()){
            game.set_rule_b(w_rule_b);
        }
        else{
            game.set_rule_b("3");
        }
        if(!w_rule_s.isEmpty()){
            game.set_rule_s(w_rule_s);
        }
        else{
            game.set_rule_s("23");
        }
        w_changed = false;
        game.third_impact();
        update();
    }
}
