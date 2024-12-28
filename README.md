# 2024_KUCIS_PROJECT
## 심플(SIEMple) 프로젝트
![SIEMple (3)](https://github.com/user-attachments/assets/a3654c21-5fc4-4f5d-9662-cda245bceaf7)

로그 데이터를 수집해서 보안 위협을 탐지, 분석하는 클라우드 SIEM 솔루션

| **🥈 2024 사이버 시큐리티 해커톤 대회 우수상** |
|------------------------------------------|

<br>

참여 인원 : 6명 (김남석, 이광호, 박선후, 김정우, 이수민, 서지운)

고객층 :현실적인 이유로 보안에 신경쓰기 어려운 대학생, 소규모 스타트업, 중소기업

목표 : 고객의 **보안 부담을 덜어 비즈니스에 온전히 집중할 수 있도록 하는 보안 솔루션**

<br>

### 기술스택

---

![Java](https://img.shields.io/badge/Java-007396)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![OpenSearch](https://img.shields.io/badge/OpenSearch-005571?style=flat-square&logo=elasticsearch&logoColor=white)

![Spring Webflux](https://img.shields.io/badge/Spring%20Webflux-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Spring Cloud AWS](https://img.shields.io/badge/Spring%20Cloud%20AWS-6DB33F?style=flat-square&logo=spring&logoColor=white)
![R2DBC](https://img.shields.io/badge/r2dbc-6DB33F?style=flat-square&logo=spring&logoColor=white)

![Terraform](https://img.shields.io/badge/Terraform-844FBA?style=flat-square&logo=terraform&logoColor=white)
![AWS Lambda](https://img.shields.io/badge/AWS%20Lambda-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![AWS WAF](https://img.shields.io/badge/AWS%20WAF-232F3E?style=flat-square&logo=waf&logoColor=white)
![AWS CloudWatch](https://img.shields.io/badge/AWS%20CloudWatch-FF4F8B?style=flat-square&logo=amazon-aws&logoColor=white)

![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![OpenHTMLtoPDF](https://img.shields.io/badge/OpenHTMLtoPDF-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![PdfBox](https://img.shields.io/badge/PdfBox-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)

<br>

### 구현된 기능

---
<div style="display: flex; justify-content: space-around;">
  <img src="https://github.com/user-attachments/assets/61125f76-8d3c-44f6-9f06-10b9580e4d2d" alt="스크린샷 1" style="width: 45%;"/>
  <img src="https://github.com/user-attachments/assets/c9cd30b0-14ae-4d93-a1e3-a7fb7f826b1f" alt="스크린샷 2" style="width: 45%;"/>
</div>

- 클라우드의 다양한 서비스를 통합하여 취약점 검출에 필요한 데이터를 수집
- 클라우드 보안 솔루션을 함께 사용해서 보안 이벤트를 직접 검출
- 실시간으로 모니터링하는 대시보드 제공, 웹서비스의 로그 조회
- ISMS-P 2.11 인증 요건에 적합한 피해 대응 보고서 자동화 (HTML, PDF)

### 상세 설명

본 제품은 ELK 기술 스택을 클라우드 상에서 구현하여 실시간 검색 및 분석을 통해 빠른 취약점 검출이 가능하며, 비용 대비 효율적인 구조로 유지보수 비용을 절감합니다.

![스크린샷 2024-12-13 155543](https://github.com/user-attachments/assets/e5030e7b-3ed5-4061-840c-0708e532354e)

**성능**

- 분산 서버에 대해서도 빠르게 작업을 수행할 수 있도록 클라우드에 특화된 구조를 설계
- 실시간 검색에 특화된 OpenSearch와 제품은 대규모 트래픽에 대해서도 속도를 유지하기 위해 비동기(Spring Webflux)로 작업을 처리
- 클라우드간의 요청시 불필요한 데이터는 정제하여 비용, 성능적 개선

**고가용성**

- 장애가 발생해도 자동으로 복구하는 클라우드의 분산 서비스를 이용해 데이터를 손실 없이 수집
- 배치 처리를 통해 작업한 위치를 기록하고 작업한 내용을 비교해 중복이 없도록 가용성 유지

기본적으로 AWS의 GarudDuty와 WAF 서비스의 검출 로직을 따르지만, AWS의 보안 서비스는 취약점 검출에 있어서 어떤 비즈니스 로직을 가지고 판별하는지 공개하지 않고 있습니다.

그래서 본 프로젝트에서는 AWS의 보안 서비스(WAF)와 함께 추가적으로 **수집한 로그 데이터를 토대로 취약점을 직접 검출해내는 로직**을 구현하고 있습니다.


### 취약점 직접 검출을 위한 모의 시뮬레이션 주입

**주요 정보통신기반 시설의 기술적 취약점 분석 평가 방법 상세 가이드**의 웹 기반 서비스의 기술 취약점 항목을 우선적으로 검출하도록 구현

<div style="display: flex; justify-content: space-around;">
  <img src="https://github.com/user-attachments/assets/830f2209-30a9-4690-bf14-4ba6f770c8b5" alt="스크린샷 3" style="width: 45%;"/>
  <img src="https://github.com/user-attachments/assets/2cb6d5dc-2c0f-4718-87f4-25f959d598a6" alt="스크린샷 4" style="width: 45%;"/>
</div>

클라우드상에 배포되는 웹서비스의 취약점들을 검출해 데이터를 수집할 수 있도록 모의 시뮬레이션용 웹서비스를 직접 구현

<br>

### 전체 이벤트의 조회와 분석

![스크린샷 2024-12-13 155529](https://github.com/user-attachments/assets/4172fc26-8c94-4430-b5a4-ffb4a90cba03)

Notion을 이용한 도움말 문서를 제공

<br>

### 편리한 보안 환경 구축

번거로운 보안 환경 구축을 개발자에게 친숙한 코드(IaC)로 필요한 리소스를 생성해서 해결

![스크린샷 2024-12-13 155601](https://github.com/user-attachments/assets/a802344b-c0c0-4de8-bbaa-165ac13459e8)

이를 통하여 고객은 클라우드에 대한 러닝 커브 없이 보안 체계를 구축할 수 있고, 비즈니스 구현에만 집중할 수 있다

<br>

### ISMS-P 2.11 규격에 맞춘 침해 대응 보고서 작성 자동화 및 커스텀 형식 제공

ISMS-P 2.11 인증 심사를 위한 프로세스를 더 편리하고 적은 비용으로 해결

![스크린샷 2024-12-13 155632](https://github.com/user-attachments/assets/5d7095b5-9b95-4099-9473-ec1c0e209256)

검출되는 취약점들에 대해서 피해 규모를 상정하고 상세 데이터들을 정리해서 isms-p 2.11 침해 대응에 필요한 규격의 보고서를 작성

pdf로 미리보기와 다운로드가 가능하며, 기본적으로 제공되는 보고서 양식 이외에도 커스텀하여 설정

html로 작성된 파일 양식을 통해 고객이 직접 원하는 형식에 맞춰서 보고서 형태를 변경

<br>

###  sLM 기반 인공지능 챗봇

소규모 언어 모델 sLM을 기반으로 하여 도메인에 특화된 생성형 인공지능 탑재

- Microsoft사에서 2024년 8월에 출시한 Phi-3.5 인공지능을 RAG 모델로 이용

<br>

![스크린샷 2024-12-13 155617](https://github.com/user-attachments/assets/5d8046b1-18f9-48f4-956f-6d2b70f23b3f)

신뢰성 있는 기관의 보고서, 취약점 설명서 및 검출을 위한 문서 수집해 ChromaDB에 기록 (KISA, SK shielders 등)

<br>

### 클라우드 비용 절감을 위한 시도

서비스 규모에 따라 OpenSearch 도메인의 클러스터 노드 개수나 인스턴스 유형이 달라질 수 있습니다.

제품에 따라 생길 수 있는 변인은 제외하고 전체적인 비용 절감을 위해 고민했었습니다.

다음 시도를 통해 성능 개선과 비용 절감 효과를 얻을 수 있었습니다.

- **불필요한 필드 제외**

```jsx
SourceFilter sourceFilter = SourceFilter.of(s -> s.includes("@id", "@log_group", "@timestamp","@message"));
SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
        );
```

OpenSearch Java Client에서 SourceFilter를 통해 ElasticSearch의 includes 문법을 구현했습니다.

⇒ 데이터 전송량을 줄여 네트워크 대역폭과 지연시간을 절약

- **검색 결과 크기 조정**

효율적인 데이터 처리를 위해 SearchRequest의 size(default=10)를 100으로 지정해서 한번에 더 많은 검색량을 받았습니다.

⇒ 서버로의 요청횟수를 줄일 수 있어 전체적인 비용 절감 효과를 기대
