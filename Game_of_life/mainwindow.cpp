#include "mainwindow.h"
#include "ui_mainwindow.h"

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::load(){
    QString file_name = QFileDialog::getOpenFileName(this,tr("Выберите файл стартовой обстановки"),QDir::homePath(), tr("GOLLY RLE format (*.rle)"));
    if(file_name.length() < 1 || !file_name.endsWith(".rle")){
        return;
    }
    QFile file(file_name);
    if (!file.open(QIODevice::ReadOnly)){
        file.close();
        return;
    }
    QTextStream in(&file);
    base_game::configs con;
    try{
        con = ui->game->game.load(in);
    }
    catch(std::exception &e){
        std::cerr << e.what();
        file.close();
        return;
    }
    ui->game->update();
    ui->b_input->setText(con.rule_b);
    ui->s_input->setText(con.rule_s);
    ui->game->set_rule_b(con.rule_b);
    ui->game->set_rule_s(con.rule_s);
    QString width = QString::number(con.width);
    ui->width_input->setText(width);
    QString height = QString::number(con.height);
    ui->height_input->setText(height);
    file.close();
}
