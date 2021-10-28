#include <gtest/gtest.h>
#include "fast.h"
#include "detailed.h"
#include "tournament.h"

class P_test : public testing::Test{
protected:
    std::vector<std::string> names;
};

TEST_F(P_test, Fast_controller_test){
    names.push_back("impostor");
    names.push_back("impostor");
    names.push_back("cooperate");
    Fast_controller test(names, 2);
    test.engage();
    auto info = test.get_info();
    EXPECT_EQ(3, info.size());
    EXPECT_EQ(10, info.at(0).score_current);
    EXPECT_EQ(10, info.at(1).score_current);
    EXPECT_EQ(0, info.at(2).score_current);
}