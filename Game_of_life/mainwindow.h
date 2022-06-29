#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include "game_widget.h"
#include <QFile>
#include <QFileDialog>
#include <QString>
#include <QTextStream>

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindow;
             class game_widget;}
QT_END_NAMESPACE

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();
public slots:
    void load();
private:
    Ui::MainWindow *ui;
    game_widget *game;
};

#endif // MAINWINDOW_H
