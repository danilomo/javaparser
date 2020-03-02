/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2020 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package com.github.javaparser.symbolsolver;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author Danilo Oliveira
 */
public class ConfigurableSymbolSolver implements SymbolResolver {

    private final SymbolResolver resolver;
    private final Map<Class<? extends Expression>, SolvingStrategy> strategies;

    public ConfigurableSymbolSolver(SymbolResolver resolver) {
        this.resolver = resolver;
        this.strategies = new HashMap<>();
    }        
    
    @Override
    public <T> T resolveDeclaration(Node node, Class<T> resultClass) {
        return resolver.resolveDeclaration(node, resultClass);
    }

    @Override
    public <T> T toResolvedType(Type javaparserType, Class<T> resultClass) {
        return resolver.toResolvedType(javaparserType, resultClass);
    }

    @Override
    public ResolvedType calculateType(Expression expr) {
        try {
            return resolver.calculateType(expr);
        } catch (Exception ex) {
            return solveWithStrategy(expr)
                    .orElse(ResolvedType.NOT_RESOLVED);
        }
    }

    public <T extends Expression> void setStrategy(
            Class<? extends Expression> type,
            SolvingStrategy<? extends Expression> strategy) {
        strategies.put(type, strategy);
    }
    
    private Optional<ResolvedType> solveWithStrategy(Expression expr) {
        SolvingStrategy strategy = strategies.get(expr.getClass());
        if(strategy == null){
            return Optional.empty();
        }
        
        return strategy.resolveType(expr);
    }    

}
