package Analysis;

public enum NTerminalS implements Symbol {

    program, if_stm, while_stm, for_stm, declare_stm, assign_stm, unary_stm, binary_stm,
    sub_program, if_post_stm, else_stm,
    expression, array_ele_stm, op, for_init_stm, array_init_var, array_post_stm, exp_post_stm,
    declare_num_post_stm, declare_bool_post_stm, statement, declare_array_stm,
    assign_num_post_stm, variable, var_post_stm,condition,
    body_stm;
    //S, SX, T, TX, F;
    @Override
    public boolean isTerminal() {
        return false;
    }

    public boolean canNil = false;

}
