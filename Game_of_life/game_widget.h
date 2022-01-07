#ifndef GAME_WIDGET_H
#define GAME_WIDGET_H

#include <QWidget>
#include <QMouseEvent>
#include <QPainter>
#include <QString>
#include <QTimer>

#include "base_game.h"

class game_widget : public QWidget
{
    Q_OBJECT
public:
    explicit game_widget(QWidget *parent = nullptr);
    size_t max_width();
    size_t max_height();
    size_t min_width();
    size_t min_height();
    base_game game;
protected:
    void paintEvent(QPaintEvent *) override;
    void mousePressEvent(QMouseEvent * e) override;
    void mouseMoveEvent(QMouseEvent * e) override;
private:
   QTimer * timer;;
   size_t w_width;
   size_t w_height;
   bool w_changed;
   QString w_rule_b;
   QString w_rule_s;
signals:
   void environment_changed(bool ok);
   void height_changed(int x);
   void width_changed(int y);
   void rule_b_changed(int b);
   void rule_s_changed(int s);
public slots:
   void start();
   void stop();
   void clear();
   size_t field_height();
   size_t field_width();
   void set_field_height(QString height);
   void set_field_width(QString width);
   void set_parametrs();
   int interval();
   void set_interval(int msec);
   void set_rule_b(QString B);
   void set_rule_s(QString S);
protected slots:
    void paint_grid(QPainter & p);
    void paint_field(QPainter & p);
    void new_iteration();
};
#endif // GAME_WIDGET_H
