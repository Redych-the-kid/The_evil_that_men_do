#include "score_table.h"

score_table::score_table() {
    defect_score.push_back(1);
    defect_score.push_back(5);
    defect_score.push_back(9);
    defect_score.push_back(-1);

    cooperate_score.push_back(-1);
    cooperate_score.push_back(0);
    cooperate_score.push_back(3);
    cooperate_score.push_back(7);
}

std::tuple<int, int, int> score_table::get_scores(size_t first, size_t second, size_t third) {
    size_t c_counter = 0; //number of cooperations
    if(first == COOPERATE) c_counter++;
    if(second == COOPERATE) c_counter++;
    if(third == COOPERATE) c_counter++;
    std::vector<int> scores;
    if(first == COOPERATE){
        scores.push_back(cooperate_score[c_counter]);
    }
    else{
        scores.push_back(defect_score[c_counter]);
    }
    if(second == COOPERATE){
        scores.push_back(cooperate_score[c_counter]);
    }
    else{
        scores.push_back(defect_score[c_counter]);
    }
    if(third == COOPERATE){
        scores.push_back(cooperate_score[c_counter]);
    }
    else{
        scores.push_back(defect_score[c_counter]);
    }
    return std::make_tuple(scores[0], scores[1], scores[2]);
}


