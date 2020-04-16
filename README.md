ALYBA (AccessLog & Your Bad Applications)
=============================
일반적인 미들웨어의 경우, 기본적으로 Accesslog를 이용하여 클라이언트 요청 정보를 기록한다. 여기에는 모든 요청에 대한 처리결과가 기록되어 있으므로, 이것을 이용하여 시스템의 성능을 분석할 수 있다. 가령, PeakTime은 언제인지, 최대 몇 TPS를 처리하는지, 응답시간 지연이 얼마나 발생하며 어떤 요청이 주로 지연되는지, 오류비율은 얼마나 되는지 등을 Accesslog를 통해 분석이 가능하다. ALYBA는 Accesslog를 분석하여 시스템의 현황을 점검하고 더 나아가 악성 어플리케이션을 식별하기 위한 목적으로 개발되었다.

Requirements
---------------
* Windows OS
* JRE 8.0 이상

Getting Started
---------------
아래의 두개 버전 중 하나를 다운로드 한다.
* Windows Executable : [ALYBA_v1.7.0.exe](https://github.com/cool8519/ALYBA/raw/master/dist/ALYBA_v1.7.0.exe)
* Java Archive File : [ALYBA_v1.7.0.jar](https://github.com/cool8519/ALYBA/raw/master/dist/ALYBA_v1.7.0.jar) [ALYBA_v1.7.0_32bit.jar](https://github.com/cool8519/ALYBA/raw/master/dist/ALYBA_v1.7.0_32bit.jar)

ALYBA 실행파일을 클릭하거나, 커맨드 창에서 실행시키면 된다.
`C:\> ALYBA.exe`

Excutable Binary 파일(exe)은 CPU 비트수에 따라 수행이 안될 수 있다. 이 경우는 JAVA Archive 파일(jar)을 아래와 같이 수행한다.

`C:\> java -jar ALYBA.jar`

결과 분석창은 메인화면에서 "Result Analyzer"버튼을 눌러서 열 수 있다. 그러나 아래와 같이 별도의 결과 분석창을 열 수 있다.

`C:\> ALYBA.exe -result [path_to_alyba_db]`

`C:\> java -jar ALYBA.jar -result [path_to_alyba_db]`

Usage
---------------
ALYBA를 사용하기 위해서는 아래의 순서를 따른다.

##### 1. 제목 입력
툴의 가장 상단에 위치한 Title 부분에 분석에 대한 제목을 입력한다.
텍스트 형태로 입력이 가능하며, 입력한 Title은 출력 결과물의 파일명에 사용된다. 그러므로 Title은 공백을 지양하고 Underscore(_) 사용을 권장한다. 동일한 파일명이 존재할 때는 “_번호”를 suffix로 붙인다. Title을 “WAS1_업무1”로 입력한 경우, 출력 파일의 이름은 “ALYBA_WAS1_업무1”이 된다.

![Screenhot](screenshots/01.jpg)

##### 2. 파일 선택
분석을 수행 할 대상 로그파일을 지정한다.
툴 상단의 Title 아래가 파일관리 부분이며, 아래의 Open Files(s), Remove Selected, Remove All 버튼을 이용하여 파일을 추가하고 제거할 수 있다. 편의를 위해서 아래의 기능을 제공한다.
* 키보드(Shift,Delete) 사용 가능
* 마우스(Drag&Drop) 사용 가능
* 파일 드래그 : 해당 파일이 추가
* 폴더 드래그 : 폴더 내 전체 파일이 추가
* <kbd>Ctrl</kbd> + 폴더 드래그 : 폴더 내 파일명이 패턴과 일치하는 파일만 추가

![Screenhot](screenshots/02.jpg)

선택된 파일을 Double-Click 함으로써 파일의 내용을 확인할 수 있으며 아래의 항목을 확인할 수 있다.
* Show headers : 파일에 헤더부분이 있을 경우, 내용과 필드 이름을 보여준다.
* Open file : 윈도우에 연결된 프로그램을 통해 파일을 오픈한다.

> Accesslog 포멧이 동일한 로그만 추가해야 하며, 파일간 포멧이 다를 경우 분석 수행과정에서 오류가 발생함

##### 3. 필드 매핑
선택한 파일을 파싱하여 분석하기 위해서는 필드 순서와 이름을 매핑해 주는 것이 필요하다.
툴의 아랫부분에서 Mapping 탭을 선택한다. 매핑은 아래의 순서로 진행한다.
* Log Type 설정
 - Accesslog 포멧이 일반적인 WEB/WAS의 기본설정인 경우, 목록에서 해당 WEB/WAS 서버를 설정한다.
 - 목록에 존재하지 않는 서버를 사용하거나, 기본 포멧이 아닌경우는 Customized로 설정한다.
* Sampling & 자동 매핑
 - Log Type을 Customized로 설정하지 않은 경우는 간단히 Sampling 버튼만 누르면 자동으로 매핑을 수행하며, 아래의 과정은 불필요하다.
* Sampling
 - Delimeter에 필드 구분자를 설정한다. 구분자가 2개 이상일 경우는 붙여서 지정하면 된다. 예) -\t ▶ “-“와 “\t”(탭)을 구분자로 파싱
 - Bracelet에 필드 괄호기호를 설정한다. 괄호기호가 2개 이상일 경우는 공백을 넣어 지정하면 된다. 예) [] "" ▶ []와 “”는 하나의 필드로 파싱
 - 대부분의 경우는 Delimeter와 Bracelet은 기본값으로 남겨두어도 좋다.
 - Sampling 버튼을 누르면 Sampling된 라인의 필드 데이터가 좌측 아랫부분에 표시된다.
* Mapping
 - 좌측의 필드 데이터를 드래그하여 오른쪽의 적절한 필드에 드롭한다.
 - Request Time은 필수 매핑 항목이며, 나머지는 선택이다.
 - 좌측의 필드 데이터에 공백으로 구분된 Delimeter가 있을 경우, 선택적으로 매핑이 가능하다.
 - <kbd>Del</kbd>키로 매핑된 필드를 취소할 수 있다.
 - 시간은 기본적으로 UTC+0 기준으로 입력되므로, 로그가 기록된 서버의 TimeZone이 있는 경우는 Hour 단위로 Offset 설정을 한다. (예: IST=5.5시간)

![Screenhot](screenshots/03.jpg)

##### 4. 필터링
필터링을 통해 전체 로그 중 일부만 내용만을 분석하는 것이 가능하다. 전체 로그를 분석하는 경우는 필터링 과정이 불필요하다.
툴의 아랫부분에서 Filter 탭을 선택한다. 필터링은 아래의 세가지 방식이 가능하다.
* Time Range : 시간범위를 지정
* Include Filter : 조건에 맞는 경우만 분석
* Exclude Filter : 조건에 맞는 경우는 분석에서 제외

필터링 조건 입력은 패턴(*,?)를 통해 입력이 가능하며, 2개 이상일 경우는 컴마(,)를 구분자로 입력한다.

![Screenhot](screenshots/04.jpg)

##### 5. 출력 지정
필요시 출력되는 파일에 대한 설정이 가능하다.
툴의 아랫부분에서 Output 탭을 선택한다. 아래의 세가지 설정이 가능하다.
* Directory : 출력되는 파일이 저장될 디렉토리 지정
* File Type : 출력되는 파일의 형식을 선택. 기본적으로 파일DB(.db) 형태로 저장되며, 추가로 필요한 출력 형식을 지정.
* Sort by : 결과물의 정렬 기준을 선택

##### 6. 옵션 설정
파싱 및 분석을 위한 옵션을 설정할 수 있다.
툴의 아랫부분에서 Option 탭을 선택한다. 옵션의 내용은 아래와 같다.
* Multi-thread Parsing : 분석할 파일의 개수가 많을 경우, Multi-Thread 방식으로 분석하여 속도가 향상됨.
* Number of fields : 라인의 별로 필드의 수가 동일한 지 체크하여 다른 경우 파싱에러로 처리
* Allow Errors : 파일당 지정된 횟수 이상 에러가 발생하면 분석 중지.
* Includes Parameters : Request URL에 Parameter가 있는지 여부를 선택. 이 옵션을 선택하면 물음표(?) 이전의 URL을 유일한 값으로 집계한다.
예) /test/index.jsp?param=data1 ▶ /test/index.jsp
* Aggregate TPM : TPM 자료를 몇 분단위로 집계할 지 설정한다.
* Elapsed time was over : 응답시간이 지정된 시간 이상인 경우 별도 수집.
* Response bytes was over : 응답크기가 지정된 크기 이상인 경우 별도 수집.
* Response code was error : 응답코드가 에러(4XX,5XX)인 경우 별도 수집.
* Aggregate TPS : 요청이 가장 많은 날의 TPS를 별도 수집.

설정한 옵션들을 파일을 통해 저장할 수 있다. 파일에는 파일과 매핑정보를 제외한 Filter, Output, Option 정보가 저장되므로, 향후에 동일한 형식의 파일을 분석할 때 저장된 기존 설정을 사용하면 편리하다.

##### 7. 분석 수행
위의 모든 설정이 완료되면 우측 상단에 Analyze 버튼이 활성화되고, 버튼을 클릭하면 설정이 맞는지 확인 후 Accesslog에 대한 실제 분석이 진행된다.

![Screenhot](screenshots/05.jpg)

분석이 완료되면 결과파일이 저장되며, 결과분석기 또는 생성된 파일을 버튼을 통해 열 수 있다.

Result Item
----------
출력 결과는 설정에 따라 Excel, HTML, Text 중 하나로 저장된다. 파일의 내용은 형식에 따라 다르지 않으며, 카테고리는 아래와 같다.
* Overview : 전체 분석 결과에 대한 개요
* TPM : 분당 트랜잭션 처리 추이를 설정한 시간 단위로 집계. 전체적인 트랜잭션의 추이를 확인할 수 있다.
* TPS : 거래량이 가장 많은 날의 초당 트랜잭션 처리 추이를 설정한 시간 단위로 집계. 이벤트와 같은 특정 날짜의 상세 트랜잭션의 추이를 확인할 수 있다.
* DAY : 일자 별 처리 통계. 주간/월간 추이 및 Peak Day를 확인할 수 있다.
* HOUR : 시간대 별 처리 통계. Peak Time을 확인할 수 있다.
* URL : 요청 URL 별 처리 통계. 많이 호출된 URL, 응답이 지연되거나 에러가 발생된 URL 등을 확인할 수 있다.
* IP : 요청 IP 별 처리 통계. 요청이 많은 IP를 확인할 수 있다. Country 정보는 GeoIP 기반의 데이터로 신뢰도가 낮으니 참고만 하는 것이 좋다.
* METHOD : 요청한 HTTP Method 별 처리 통계. 요청이 많은 Method를 확인할 수 있다.
* EXT : 요청한 URL의 확장자 별 처리 통계. 정적/동적 컨텐츠 처리 현황을 확인할 수 있다.
* CODE : HTTP 응답코드 별 통계. 오류발생 통계를 확인할 수 있다.
* RES_TIME : 처리시간이 설정한 시간을 초과한 건. 응답시간이 느린 악성 어플리케이션을 확인할 수 있다.
* RES_BYTE : 응답크기가 설정한 크기를 초과한 건. 응답크기가 큰 악성 어플리케이션을 확인할 수 있다.
* ERROR : 에러(4XX, 5XX) 발생 건. 에러가 발생했던 이력을 확인할 수 있다

Result Analyzer
----------
결과분석기를 통해 파싱된 결과를 데이터 또는 그래프 형태로 확인할 수 있다.
아래의 3가지 뷰를 제공한다.

##### Summary Tab
분석된 결과의 요약된 내용이다.
분석시간, 필터링 정보, 집계된 데이터 건수, Peak 시점, 비정상(시간/크기/에러) 건수를 확인할 수 있다.

![Screenhot](screenshots/06.jpg)

##### Data Tab
분석 항목별 데이터를 테이블 형태로 확인할 수 있다.

![Screenhot](screenshots/07.jpg)

조회 조건을 지정하여 쿼리할 수 있으며, 쿼리 형식은 표준 SQL의 WHERE 절 구문을 사용할 수 있다.  컬럼명 앞에는 테이블을 의미하는 "t." 을 prefix로 붙이면 된다.
> 조건절 예) t.req_count > 100 and t.err_count = 0

Export 버튼을 통해 CSV 형식으로 데이터를 내려받을 수 있다. 전체 데이터 또는 분석기에 보이는 데이터 중 하나를 선택하면 된다. 데이터가 많은 경우, 전체 데이터를 내려 받으면 프로그램이 느려질 수 있으므로 주의한다. 분석기 화면의 데이터는 마우스 스크롤 또는 <kbd>PgDn</kbd>키를 통해 추가로 가져올 수 있다.

선택된 컬럼에서 <kbd>Ctrl</kbd> + <kbd>c</kbd>를 누르면 해당 컬럼의 데이터가 클립보드에 복사된다.


##### Chart Tab
분석된 결과를 그래프로 볼 수 있다.
수집된 데이터에 따라 볼 수 있는 차트가 다르며, 필요한 데이터가 없는 경우는 목록에서 해당 차트는 표시되지 않는다.

좌측 Data의 콤보박스 중 데이터를 선택하면, 우측에 관련 그래프가 표시된다.
집계된 데이터의 타입에 따라 가능한 차트 종류를 선택할 수 있다.
* 시계열 데이터 : Line Chart
* 키값 데이터 : Vertical Bar Chart, Horizontal Bar Chart, Pie Chart
* 산포 데이터 : Vertical Bar Chart, Horizontal Bar Chart, Pie Chart, Plot Chart

![Screenhot](screenshots/08.jpg)
![Screenhot](screenshots/09.jpg)
![Screenhot](screenshots/10.jpg)
![Screenhot](screenshots/11.jpg)
![Screenhot](screenshots/12.jpg)

Resource Tab에서 자원사용률 데이터를 추가하면 아래와 같은 차트를 선택할 수 있다.

![Screenhot](screenshots/14.jpg)
![Screenhot](screenshots/15.jpg)

그래프 영역에서 특정 데이터를 클릭하거나 드래그하여 상세하게 확인할 수 있다.

##### Resource Tab (v1.7.0 추가)
자원사용률 로그를 분석 결과에 추가한다.
Accesslog와 동일 시간대의 데이터인 경우, 회귀 분석을 통해 미래예측 및 용량산정을 수행할 수 있다.

Accesslog 파싱 과정과 동일하게 아래의 과정을 따른다.

1. 파일 선택 : 리소스 로그 파일을 지정한다. 파일에 Server명과 Group명을 지정하여 서버 및 그룹별로 분석할 수 있다.
2. 필드 매핑 : 파일 타입은 vmstat, sar, customize를 지원한다.
 - vmstat : `vmstat -t <interval_secs>` 로 수집된 로그
 - sar : `sar -u <interval_secs>` 로 수집된 로그
 - customize : 임의 형식의 텍스트 로그. 사용자 지정 필드 매핑을 해야 한다.
3. 분석 수행 : 필드 매핑이 완료되면 Analyze 버튼이 활성화되고, 버튼을 클릭하면 분석이 시작된다. 완료되면 좌측 상단에 추가된 자료의 목록이 표시된다.

![Screenhot](screenshots/13.jpg)

리소스사용률 데이터가 추가되면 Chart 탭에 System Resource와 Regression Analysis 그래프가 추가된다.(DB파일 reload 필요)

Release Note
--------------
##### v1.5.1
- (Bug) 필드 매핑시 TIME에 날짜가 없이 시간만 있는 경우 인식이 안되는 문제
- 로그파일 Encoding 자동 인식

##### v1.6.0
- 매핑정보를 설정파일에 저장 가능
- 설정파일 Drag & Drop
- Table에 복합데이터 내 상세데이터 출력
- Table 선택시 불필요한 컬럼을 자동으로 숨김
- 산포도에서 점 클릭시 상세 데이터 표시
- 산포도에서 분포 그래프 지원(경계값 변경 가능)
- IP 지역 기반 그래프 지원

##### v1.7.0
- 버그 수정
- 파일엔코딩 확인처리 성능 개선
- 자원사용률 로그 파싱 및 그래프 추가
- 회귀분석 기능 추가

To-do
--------------
What ALYBA will implement in the future:
* Analysis History Management
* Non-linear Regression Analysis
* Customized Visualization
* Symentic URL support

