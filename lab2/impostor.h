//
// Created by opezdal on 21.10.2021.
//

#ifndef LAB2_IMPOSTOR_H
#define LAB2_IMPOSTOR_H
#include "strategy.h"

class Impostor : public Strategy{
public:
    virtual size_t decide();
};

#endif //LAB2_IMPOSTOR_H
