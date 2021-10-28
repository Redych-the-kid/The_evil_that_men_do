#include "controller.h"

Controller::Controller(const std::vector<std::string> &names, size_t steps) {
    this->steps = steps;
    for(std::string name:names){
        add_strategy(name);
    }
}

void Controller::print_state() {
    for(Information info : strategy_info){
        std::cout << info.name << ":" << info.score_current << std::endl;
    }
}

void Controller::add_strategy(const std::string &name) {
    std::shared_ptr<Strategy> strategy(Factory<Strategy, std::string>::get_instance()->create(name));
    Information info(strategy);
    info.name = name;
    strategy_info.push_back(info);
}

void Controller::print_winner() {
    const Information * max_strategy = nullptr;
    for(size_t i = 0; i < strategy_info.size(); ++i){
        if(max_strategy == nullptr || strategy_info.at(i).score_current > max_strategy->score_current){
            max_strategy = &strategy_info.at(i);
        }
    }
    std::cout << "The winner is:" << max_strategy->name << "(" << max_strategy->score_current << ")" << std::endl;
}

void Controller::do_iteration(size_t first, size_t second, size_t third) {
    std::shared_ptr<Strategy> s_first = strategy_info.at(first).strategy;
    std::shared_ptr<Strategy> s_second = strategy_info.at(second).strategy;
    std::shared_ptr<Strategy> s_third = strategy_info.at(third).strategy;

    size_t d_first = s_first->decide();
    size_t d_second = s_second->decide();
    size_t d_third = s_third->decide();

    score_table table;
    auto scores = table.get_scores(d_first, d_second, d_third);

    s_first->add_enemy_desicions(d_second, d_third);
    s_second->add_enemy_desicions(d_first,d_third);
    s_third->add_enemy_desicions(d_first, d_second);

    strategy_info.at(first).score_current += std::get<0>(scores);
    strategy_info.at(second).score_current += std::get<1>(scores);
    strategy_info.at(third).score_current += std::get<2>(scores);

    strategy_info.at(first).score_last = std::get<0>(scores);
    strategy_info.at(second).score_last = std::get<1>(scores);
    strategy_info.at(third).score_last = std::get<2>(scores);

    strategy_info.at(first).choice_last = d_first;
    strategy_info.at(second).choice_last = d_second;
    strategy_info.at(third).choice_last = d_third;
}

std::vector<Information> Controller::get_info() {
    return strategy_info;
}