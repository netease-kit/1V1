// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.model;

import java.io.Serializable;
import java.util.List;

public class YidunAntiSpamResModel implements Serializable {

  private ExtBean ext;

  public ExtBean getExt() {
    return ext;
  }

  public void setExt(ExtBean ext) {
    this.ext = ext;
  }

  public static class ExtBean implements Serializable {
    private AntispamBean antispam;

    public AntispamBean getAntispam() {
      return antispam;
    }

    public void setAntispam(AntispamBean antispam) {
      this.antispam = antispam;
    }

    public static class AntispamBean implements Serializable {
      private String dataId;
      private int suggestion;
      private int censorType;
      private boolean isRelatedHit;
      private long censorTime;
      private int resultType;
      private String taskId;
      private List<LabelsBean> labels;

      public String getDataId() {
        return dataId;
      }

      public void setDataId(String dataId) {
        this.dataId = dataId;
      }

      public int getSuggestion() {
        return suggestion;
      }

      public void setSuggestion(int suggestion) {
        this.suggestion = suggestion;
      }

      public int getCensorType() {
        return censorType;
      }

      public void setCensorType(int censorType) {
        this.censorType = censorType;
      }

      public boolean isIsRelatedHit() {
        return isRelatedHit;
      }

      public void setIsRelatedHit(boolean isRelatedHit) {
        this.isRelatedHit = isRelatedHit;
      }

      public long getCensorTime() {
        return censorTime;
      }

      public void setCensorTime(long censorTime) {
        this.censorTime = censorTime;
      }

      public int getResultType() {
        return resultType;
      }

      public void setResultType(int resultType) {
        this.resultType = resultType;
      }

      public String getTaskId() {
        return taskId;
      }

      public void setTaskId(String taskId) {
        this.taskId = taskId;
      }

      public List<LabelsBean> getLabels() {
        return labels;
      }

      public void setLabels(List<LabelsBean> labels) {
        this.labels = labels;
      }

      public static class LabelsBean implements Serializable {
        private List<SubLabelsBean> subLabels;
        private int level;
        private int label;

        public List<SubLabelsBean> getSubLabels() {
          return subLabels;
        }

        public void setSubLabels(List<SubLabelsBean> subLabels) {
          this.subLabels = subLabels;
        }

        public int getLevel() {
          return level;
        }

        public void setLevel(int level) {
          this.level = level;
        }

        public int getLabel() {
          return label;
        }

        public void setLabel(int label) {
          this.label = label;
        }

        public static class SubLabelsBean implements Serializable {
          private String subLabel;
          private DetailsBean details;

          public String getSubLabel() {
            return subLabel;
          }

          public void setSubLabel(String subLabel) {
            this.subLabel = subLabel;
          }

          public DetailsBean getDetails() {
            return details;
          }

          public void setDetails(DetailsBean details) {
            this.details = details;
          }

          public static class DetailsBean implements Serializable {
            private List<HitInfosBean> hitInfos;
            private List<KeywordsBean> keywords;

            public List<HitInfosBean> getHitInfos() {
              return hitInfos;
            }

            public void setHitInfos(List<HitInfosBean> hitInfos) {
              this.hitInfos = hitInfos;
            }

            public List<KeywordsBean> getKeywords() {
              return keywords;
            }

            public void setKeywords(List<KeywordsBean> keywords) {
              this.keywords = keywords;
            }

            public static class HitInfosBean implements Serializable {
              private List<PositionsBean> positions;
              private String value;

              public List<PositionsBean> getPositions() {
                return positions;
              }

              public void setPositions(List<PositionsBean> positions) {
                this.positions = positions;
              }

              public String getValue() {
                return value;
              }

              public void setValue(String value) {
                this.value = value;
              }

              public static class PositionsBean implements Serializable {
                private String fieldName;
                private int startPos;
                private int endPos;

                public String getFieldName() {
                  return fieldName;
                }

                public void setFieldName(String fieldName) {
                  this.fieldName = fieldName;
                }

                public int getStartPos() {
                  return startPos;
                }

                public void setStartPos(int startPos) {
                  this.startPos = startPos;
                }

                public int getEndPos() {
                  return endPos;
                }

                public void setEndPos(int endPos) {
                  this.endPos = endPos;
                }
              }
            }

            public static class KeywordsBean implements Serializable {
              private String word;

              public String getWord() {
                return word;
              }

              public void setWord(String word) {
                this.word = word;
              }
            }
          }
        }
      }
    }
  }
}
