#ifndef LAB2_SCORE_TABLE_H
#define LAB2_SCORE_TABLE_H

#include <iostream>
#include <tuple>
#include <vector>
#include <string>

#define COOPERATE 1
#define DEFECT 0

class score_table{
public:
    score_table();
    std::tuple<int, int, int>get_scores(size_t first, size_t second, size_t third);
private:
    std::vector<int> cooperate_score;
    std::vector<int> defect_score;
};

#endif //LAB2_SCORE_TABLE_H
