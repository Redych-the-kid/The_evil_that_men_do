#include "tournament.h"

void Tournament_controller::tournament_tick() {
    for(size_t i = 0;i < strategy_info.size() - 2;++i){
        for(size_t j = i + 1;j < strategy_info.size() - 1;++j){
            for(size_t k = j + 1;k < strategy_info.size(); ++k){
                std::cout << "Matchup:" << strategy_info.at(i).name << " vs " << strategy_info.at(j).name << " vs " << strategy_info.at(k).name << std::endl;
                this->do_iteration(i, j, k);
                this->print_state();
            }
        }
    }
}

void Tournament_controller::engage() {
    for(size_t i = 0;i < steps;++i){
        tournament_tick();
    }
}