#ifndef LAB2_TIT4TAT_H
#define LAB2_TIT4TAT_H
#include "strategy.h"

class Tit4tat : public Strategy{
public:
    virtual size_t decide();
    virtual void add_enemy_desicions(size_t first, size_t second);
private:
    bool betrayal_happend = false;
};
#endif
