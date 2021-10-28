#ifndef LAB2_STRATEGY_H
#define LAB2_STRATEGY_H

#include <iostream>
#include "score_table.h"

class Strategy{
public:
    virtual size_t decide() = 0;
    //virtual void load_config(){}; // coming soon!
    virtual ~Strategy(){};
    virtual void add_enemy_desicions(size_t first, size_t second){};
};
#endif
