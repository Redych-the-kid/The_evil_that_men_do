#ifndef LAB2_IMPOSTOR_H
#define LAB2_IMPOSTOR_H

#include "strategy.h"

class Impostor : public Strategy{
public:
    size_t decide() override;
};

#endif //LAB2_IMPOSTOR_H
