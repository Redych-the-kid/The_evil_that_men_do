#include <gtest/gtest.h>
#include "fast.h"
#include "detailed.h"
#include "tournament.h"
#include "arg_parser.h"

class P_test : public testing::Test {
protected:
    std::vector<std::string> names;
};

TEST_F(P_test, Fast_controller_test) {
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

TEST_F(P_test, Detailed_controller_test) {
    names.push_back("impostor");
    names.push_back("impostor");
    names.push_back("cooperate");
    Detailed_controller test(names, 2);
    for (size_t i = 0; i < 2; ++i) {
        test.test_engage();
    }
    auto info = test.get_info();
    EXPECT_EQ(3, info.size());
    EXPECT_EQ(10, info.at(0).score_current);
    EXPECT_EQ(10, info.at(1).score_current);
    EXPECT_EQ(0, info.at(2).score_current);
}

TEST_F(P_test, Tournament_controller_test){
    names.push_back("cooperate");
    names.push_back("cooperate");
    names.push_back("impostor");
    names.push_back("impostor");
    Tournament_controller test(names, 1);
    test.engage();
    auto info = test.get_info();
    EXPECT_EQ(4, info.size());
    EXPECT_EQ(6, info.at(0).score_current);
    EXPECT_EQ(6, info.at(1).score_current);
    EXPECT_EQ(19, info.at(2).score_current);
    EXPECT_EQ(19, info.at(3).score_current);
}

TEST_F(P_test, Arg_parser_test){
    char *test_argv[6] = {(char *)"",
                          (char *)"-mode=detailed",
                          (char *)"-steps=100500",
                          (char *)"test1",
                          (char *)"test2",
                          (char *)"test3"};
    Arg_parser arg_parser(6, test_argv);
    EXPECT_EQ(Game_modes::detailed, arg_parser.mode);
    EXPECT_EQ(100500,arg_parser.steps);
    EXPECT_EQ(3,arg_parser.strategy_names.size());
    EXPECT_EQ("test1",arg_parser.strategy_names.at(0));
    EXPECT_EQ("test2",arg_parser.strategy_names.at(1));
    EXPECT_EQ("test3",arg_parser.strategy_names.at(2));
}