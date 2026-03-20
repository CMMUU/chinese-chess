package cn.chinesechess.core.rules;

import cn.chinesechess.core.IMoveRule;
import cn.chinesechess.core.PieceType;

import java.util.List;

/**
 * 走棋规则工厂，管理所有走棋规则策略
 * <p>根据棋子类型分发到对应的走棋规则实现。</p>
 */
public class MoveRuleFactory {

    /** 所有走棋规则实现 */
    private static final List<IMoveRule> RULES = List.of(
            new KingMoveRule(),
            new AdvisorMoveRule(),
            new ElephantMoveRule(),
            new RookMoveRule(),
            new KnightMoveRule(),
            new CannonMoveRule(),
            new PawnMoveRule()
    );

    private MoveRuleFactory() {
        // 工具类，禁止实例化
    }

    /**
     * 根据棋子类型获取对应的走棋规则
     * @param type 棋子类型
     * @return 对应的走棋规则
     * @throws IllegalArgumentException 如果找不到匹配的规则
     */
    public static IMoveRule getRule(PieceType type) {
        for (IMoveRule rule : RULES) {
            if (rule.isApplicable(type)) {
                return rule;
            }
        }
        throw new IllegalArgumentException("未找到棋子类型 " + type + " 的走棋规则");
    }
}
