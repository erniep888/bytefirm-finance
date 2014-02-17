/*
 * Copyright 2014 Byte Firm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytefirm.finance.domain


import javax.persistence.*
import org.hibernate.cfg.*
/**
 * Created by ernes_000 on 2/16/14.
 */
@Entity
class MonthlyWhereClause {
    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="monthlywhereclause_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="pk_sequence")
    public Long id
    @OneToOne
    public Account account
    @Column(length = 2500)
    public String whereClause
    public Double amountPercentage
    public Integer month
    public Integer year

    String toString() { "$account - $whereClause - $amountPercentage - $month / $year" }
}
