#!/bin/bash

QCIR_TESTSET="
  qbfEval17_qcir/c3_Debug_s3_f2_e2_v2.qcir
  qbfEval17_qcir/pipesnotankage18_8.qcir
  qbfEval17_qcir/SR-sat-03-01-07-3.qcir
  qbfEval17_qcir/p20-20.pddl_planlen=23.qcir
  qbfEval17_qcir/pipesnotankage14_10.qcir
  qbfEval17_qcir/vonNeumann-ripple-carry-12-c.qcir
  qbfEval17_qcir/CM-sat-17-01-06-4.qcir
  qbfEval17_qcir/dungeon_i25-m12-u3-v0.pddl_planlen=190.qcir
  qbfEval17_qcir/dungeon_i25-m12-u3-v0.pddl_planlen=165.qcir
  qbfEval17_qcir/SR-sat-04-01-08-2.qcir
  qbfEval17_qcir/SR-unsat-04-01-07-2.qcir
  qbfEval17_qcir/ev-pr-6x6-17-5-0-1-2-s.qcir
  qbfEval17_qcir/c2_BMC_p1_k2048.qcir
  qbfEval17_qcir/c4_Debug_s5_f2_e2_v1.qcir
  qbfEval17_qcir/c4_Debug_s3_f2_e2_v2.qcir
  qbfEval17_qcir/ev-pr-6x6-13-5-0-1-2-s.qcir
  qbfEval17_qcir/CM-sat-17-01-07-3.qcir
  qbfEval17_qcir/c2_Debug_s3_f1_e1_v2.qcir
  qbfEval17_qcir/c1_Debug_s5_f1_e1_v2.qcir
"

QDIMACS_TESTSET="
  2QBF/AR-fixpoint-10.qdimacs
  2QBF/AR-fixpoint-8.qdimacs
  2QBF/AR-fixpoint-6.qdimacs
  2QBF/AR-fixpoint-4.qdimacs
  2QBF/6s289rb05233_c0to63.sat.qdimacs
  2QBF/AR-fixpoint-2.qdimacs
  2QBF/ltl2dba_E6_comp2_REAL.sat.qdimacs
  2QBF/mult_bool_matrix_18_18_18.sat.qdimacs
  2QBF/oski3ub5i_c0to63.sat.qdimacs
  PCNF/c1_BMC_p2_k2048.qdimacs
  PCNF/c1_BMC_p1_k2048.qdimacs
  PCNF/c1_BMC_p2_k1024.qdimacs
  PCNF/c3_Debug_s3_f2_e2_v2.qdimacs
  PCNF/c3_Debug_s3_f2_e2_v3.qdimacs
  PCNF/c5_BMC_p2_k1024.qdimacs
  PCNF/AR-fixpoint-9.qdimacs
  PCNF/pipesnotankage19_9.qdimacs
  PCNF/c2_BMC_p1_k2048.qdimacs
  RANDOM/hex_hein_7x7_18.qdimacs
  RANDOM/Q_3-3-3_v-100-100-100_r-70.0.qdimacs
"

cd /tmp
wget http://www.qbflib.org/eval17.zip
unzip eval17.zip
mv eval17 qbfeval17
cd qbfeval17
tar xf qbfeval17_qcir.tar
tar xf qbfeval17_qdimacs.tar
for f in $QCIR_TESTSET $QDIMACS_TESTSET; do echo $f && gunzip $f.gz && mv $f .; done
rm -rf RAW_RESULTS.zip qbfeval17* 2QBF PCNF RANDOM qbfEval17_qcir
