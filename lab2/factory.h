#ifndef LAB2_FACTORY_H
#define LAB2_FACTORY_H
#include <iostream>
#include <map>

template<class Product, class Id>
class Factory{
private:
    std::map<Id, Product * (*)()> creators;
public:
    typedef Product * (* Creator)();
    static Factory* get_instance() {
        static Factory<Product,Id> f;
        return &f;
    }
    Product * create(const Id &id){
        return creators[id]();
    }
    bool register_creator(const Id &id, const Creator & creator){
        creators[id] = creator;
        return true;
    }
};
#endif
