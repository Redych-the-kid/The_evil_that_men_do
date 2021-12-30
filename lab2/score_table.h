#ifndef LAB2_SCORE_TABLE_H
#define LAB2_SCORE_TABLE_H

#include <tuple>
#include <vector>

constexpr std::size_t COOPERATE = 1;
constexpr std::size_t DEFECT = 0;

class score_table{
public:
    score_table();
    std::tuple<int, int, int>get_scores(std::size_t first, std::size_t second, std::size_t third);
private:
    std::vector<int> cooperate_score;
    std::vector<int> defect_score;
};

#endif //LAB2_SCORE_TABLE_H
