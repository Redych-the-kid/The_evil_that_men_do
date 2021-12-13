#ifndef LAB2_CONTROLLER_H
#define LAB2_CONTROLLER_H

#include "factory.h"
#include "strategy.h"
#include "score_table.h"
#include <memory>

struct Information{
    Information(std::shared_ptr<Strategy> strategy){
        this->strategy = strategy;
    }
    std::shared_ptr<Strategy> strategy; //          ?????????????????????????
    size_t score_current = 0;
    size_t score_last = 0;
    size_t choice_last;
    std::string name;
};

class Controller{
public:
    Controller(const std::vector<std::string> &names, size_t steps);
    virtual void engage() = 0;
    void print_state();
    void print_winner();
    std::vector<Information> get_info();
protected:
    size_t steps;
    void do_iteration(size_t first, size_t second, size_t third);
    std::vector<Information>strategy_info;
private:
    void add_strategy(const std::string &name);
};
#endif //LAB2_CONTROLLER_H
