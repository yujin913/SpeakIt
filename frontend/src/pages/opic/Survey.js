// src/pages/opic/Survey.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Survey.css';

const Survey = () => {
  const navigate = useNavigate();

  // which section we’re on: 1~4
  const [section, setSection] = useState(1);

  // Part 1 state…
  const [industry, setIndustry] = useState(null);
  const [teachingPlace, setTeachingPlace] = useState(null);
  const [hasJob, setHasJob] = useState(null);
  const [workPeriod, setWorkPeriod] = useState(null);
  const [managementRole, setManagementRole] = useState(null);

  // Part 2 state…
  const [isStudent, setIsStudent] = useState(null);
  const [recentCourse, setRecentCourse] = useState(null);

  // Part 3 state
  const [residence, setResidence] = useState(null);

  // Part 4 state
  const [leisure, setLeisure] = useState([]);
  const [hobbies, setHobbies] = useState([]);
  const [sports, setSports] = useState([]);
  const [travel, setTravel] = useState([]);

  // checkbox toggle helper
  const toggle = (arr, setter, val) => {
    setter(arr.includes(val) ? arr.filter(x => x !== val) : [...arr, val]);
  };

  // --- PART 1 helpers & completion checks ---
  const bizIndustries = ['사업/회사','재택근무/재택사업'];
  const bizLong = ['첫직장 - 2개월 이상','첫직장 아님 - 경험 많음'];

  const isBizLong = bizIndustries.includes(industry) && hasJob && bizLong.includes(workPeriod);
  const isTeachLong = industry==='교사/교육자' && hasJob && workPeriod==='2개월 이상';
  const showMgmt = isBizLong || isTeachLong;

  const complete1 =
    industry && (
      // no‑follow‑ups
      ['군 복무','일 경험 없음'].includes(industry) ||
      // business flow
      (bizIndustries.includes(industry) && hasJob!=null && (!hasJob || workPeriod)) ||
      // teacher flow
      (industry==='교사/교육자' && teachingPlace && hasJob!=null && (!hasJob || workPeriod))
    ) &&
    (!showMgmt || managementRole!=null);

  // --- PART 2 completion ---
  const complete2 = isStudent!=null && (!isStudent || recentCourse);

  // --- PART 3 completion ---
  const complete3 = !!residence;

  // --- PART 4 completion ---
  const totalCount = leisure.length + hobbies.length + sports.length + travel.length;
  const complete4 =
    leisure.length>=2 &&
    hobbies.length>=1 &&
    sports.length>=1 &&
    travel.length>=1 &&
    totalCount>=12;

  // decide if Next/Proceed should be enabled
  let canNext = false;
  if (section===1) canNext = complete1;
  else if (section===2) canNext = complete2;
  else if (section===3) canNext = complete3;
  else if (section===4) canNext = complete4;

  // handleBack & handleNext
  const handleBack = () => {
    if (section>1) setSection(section-1);
    else navigate(-1);
  };
  const handleNext = () => {
    if (section < 4) {
      setSection(section+1);
    } else {
      navigate('/opic/self-assessment');
    }
  };

  return (
    <div className="survey-page">
      {/* Step bar */}
      <ul className="step-indicator">
        {[1,2,3,4,5].map(i => (
          <li key={i} className={i<=section?'active':''}>
            Step {i}<br/><span>{
              ['Background Survey','Self Assessment','Setup','Sample Question','Begin Test'][i-1]
            }</span>
          </li>
        ))}
      </ul>

      <h2>Background Survey</h2>
      <p>질문을 읽고 정확히 답변해 주시기 바랍니다. 설문에 대한 응답을 기초로 개인별 문항이 출제됩니다.</p>

      {/* PART 1 */}
      {section===1 && (
        <>
          <h3>Part 1 of 4</h3>
          {/* Industry */}
          <div className="question-group">
            <p className="question">현재 귀하는 어느 분야에 종사하고 계십니까?</p>
            {['사업/회사','재택근무/재택사업','교사/교육자','군 복무','일 경험 없음']
              .map(opt=>(
              <label key={opt} className="radio-label">
                <input
                  type="radio"
                  name="industry"
                  value={opt}
                  checked={industry===opt}
                  onChange={()=>{
                    setIndustry(opt);
                    setTeachingPlace(null);
                    setHasJob(null);
                    setWorkPeriod(null);
                    setManagementRole(null);
                  }}
                />
                {opt}
              </label>
            ))}
          </div>
          {/* Business or Teacher follow‑ups... */}
          {bizIndustries.includes(industry) && (
            <>
              {/* hasJob */}
              <div className="question-group">
                <p className="question">현재 귀하는 직업이 있으십니까?</p>
                {['예','아니오'].map(opt=>(
                  <label key={opt} className="radio-label">
                    <input
                      type="radio"
                      name="hasJob"
                      checked={hasJob=== (opt==='예')}
                      onChange={()=>{
                        const yes = opt==='예';
                        setHasJob(yes);
                        if(!yes) setWorkPeriod(null);
                        setManagementRole(null);
                      }}
                    />
                    {opt}
                  </label>
                ))}
              </div>
              {/* workPeriod */}
              {hasJob && (
                <div className="question-group">
                  <p className="question">귀하의 근무 기간은 얼마나 되십니까?</p>
                  {['첫직장 - 2개월 미만','첫직장 - 2개월 이상','첫직장 아님 - 경험 많음']
                    .map(opt=>(
                    <label key={opt} className="radio-label">
                      <input
                        type="radio"
                        name="workPeriod"
                        checked={workPeriod===opt}
                        onChange={()=>{
                          setWorkPeriod(opt);
                          setManagementRole(null);
                        }}
                      />
                      {opt}
                    </label>
                  ))}
                </div>
              )}
            </>
          )}
          {industry==='교사/교육자' && (
            <>
              {/* teachingPlace */}
              <div className="question-group">
                <p className="question">현재 귀하는 어디에서 학생을 가르치십니까?</p>
                {['대학 이상','초등/중/고등학교','평생교육']
                  .map(opt=>(
                  <label key={opt} className="radio-label">
                    <input
                      type="radio"
                      name="teachingPlace"
                      checked={teachingPlace===opt}
                      onChange={()=>{
                        setTeachingPlace(opt);
                        setHasJob(null);
                        setWorkPeriod(null);
                        setManagementRole(null);
                      }}
                    />
                    {opt}
                  </label>
                ))}
              </div>
              {/* teacher hasJob */}
              {teachingPlace && (
                <div className="question-group">
                  <p className="question">현재 귀하는 직업이 있으십니까?</p>
                  {['예','아니오'].map(opt=>(
                    <label key={opt} className="radio-label">
                      <input
                        type="radio"
                        name="hasJob"
                        checked={hasJob=== (opt==='예')}
                        onChange={()=>{
                          const yes = opt==='예';
                          setHasJob(yes);
                          if(!yes) setWorkPeriod(null);
                          setManagementRole(null);
                        }}
                      />
                      {opt}
                    </label>
                  ))}
                </div>
              )}
              {/* teacher workPeriod */}
              {hasJob && (
                <div className="question-group">
                  <p className="question">귀하의 근무 기간은 얼마나 되십니까?</p>
                  {['2개월 미만 - 첫직장','2개월 미만 - 교직 첫 경험이지만 이전에 다른 직업을 가진 적이 있음','2개월 이상']
                    .map(opt=>(
                    <label key={opt} className="radio-label">
                      <input
                        type="radio"
                        name="workPeriod"
                        checked={workPeriod===opt}
                        onChange={()=>{
                          setWorkPeriod(opt);
                          setManagementRole(null);
                        }}
                      />
                      {opt}
                    </label>
                  ))}
                </div>
              )}
            </>
          )}
          {/* management question */}
          {showMgmt && (
            <div className="question-group">
              <p className="question">귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?</p>
              {['예','아니요'].map(opt=>(
                <label key={opt} className="radio-label">
                  <input
                    type="radio"
                    name="managementRole"
                    checked={managementRole===opt}
                    onChange={()=>setManagementRole(opt)}
                  />
                  {opt}
                </label>
              ))}
            </div>
          )}
        </>
      )}

      {/* PART 2 */}
      {section===2 && (
        <>
          <h3>Part 2 of 4</h3>
          <div className="question-group">
            <p className="question">현재 당신은 학생입니까?</p>
            {['예','아니요'].map(opt=>(
              <label key={opt} className="radio-label">
                <input
                  type="radio"
                  name="isStudent"
                  checked={isStudent=== (opt==='예')}
                  onChange={()=> {
                    const yes = opt==='예';
                    setIsStudent(yes);
                    if(!yes) setRecentCourse(null);
                  }}
                />
                {opt}
              </label>
            ))}
          </div>
          {isStudent!=null && (
            <div className="question-group">
              <p className="question">최근 어떤 강의를 수강했습니까?</p>
              {['학위 과정 수업','전문 기술 향상을 위한 평생 학습','어학수업','수강 후 5년 이상 지남']
                .map(opt=>(
                <label key={opt} className="radio-label">
                  <input
                    type="radio"
                    name="recentCourse"
                    checked={recentCourse===opt}
                    onChange={()=>setRecentCourse(opt)}
                  />
                  {opt}
                </label>
              ))}
            </div>
          )}
        </>
      )}

      {/* PART 3 */}
      {section===3 && (
        <>
          <h3>Part 3 of 4</h3>
          <div className="question-group">
            <p className="question">현재 귀하는 어디에 살고 계십니까?</p>
            {[
              '개인주택이나 아파트에 홀로 거주',
              '친구나 룸메이트와 함께 거주',
              '가족(배우자/자녀/기타 가족 일원)과 함께 주택이나 아파트에 거주',
              '학교 기숙사',
              '군대 막사'
            ].map(opt=>(
              <label key={opt} className="radio-label">
                <input
                  type="radio"
                  name="residence"
                  checked={residence===opt}
                  onChange={()=>setResidence(opt)}
                />
                {opt}
              </label>
            ))}
          </div>
        </>
      )}

      {/* PART 4 */}
      {section===4 && (
        <>
          <h3>Part 4 of 4</h3>
          <p>총 12개 이상 선택하세요.</p>

          {/** Leisure Activities (min 2) **/}
          <div className="question-group">
            <p className="question">여가 활동 (2개 이상)</p>
            <p className="selection-count">{leisure.length} selected</p>
            {['영화보기','클럽/나이트클럽 가기','공연보기','콘서트보기','박물관가기','공원가기','캠핑하기','해변가기','스포츠 관람','주거 개선']
              .map(opt=>(
              <label key={opt} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={leisure.includes(opt)}
                  onChange={()=>toggle(leisure,setLeisure,opt)}
                />
                {opt}
              </label>
            ))}
          </div>

          {/** Hobbies **/}
          <div className="question-group">
            <p className="question">취미/관심사 (1개 이상)</p>
            <p className="selection-count">{hobbies.length} selected</p>
            {['아이에게 책 읽어주기','음악 감상하기','악기 연주하기','혼자 노래부르거나 합창하기', '춤추기', '글쓰기(편지, 단문, 시 등)', '그림 그리기', '요리하기','애완동물 기르기']
              .map(opt=>(
              <label key={opt} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={hobbies.includes(opt)}
                  onChange={()=>toggle(hobbies,setHobbies,opt)}
                />
                {opt}
              </label>
            ))}
          </div>

          {/** Sports **/}
          <div className="question-group">
            <p className="question">운동 (1개 이상)</p>
            <p className="selection-count">{sports.length} selected</p>
            {['농구','야구/소프트볼','축구','미식축구','하키','크리켓','골프','배구','테니스','배드민턴','탁구','수영','자전거','스키/스노우보드','아이스 스케이트','조깅','걷기','요가','하이킹/트레킹','낚시','헬스','운동을 전혀 하지 않음']
              .map(opt=>(
              <label key={opt} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={sports.includes(opt)}
                  onChange={()=>toggle(sports,setSports,opt)}
                />
                {opt}
              </label>
            ))}
          </div>

          {/** Travel **/}
          <div className="question-group">
            <p className="question">휴가/출장 (1개 이상)</p>
            <p className="selection-count">{travel.length} selected</p>
            {['국내출장','해외출장','집에서 보내는 휴가','국내 여행','해외 여행']
              .map(opt=>(
              <label key={opt} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={travel.includes(opt)}
                  onChange={()=>toggle(travel,setTravel,opt)}
                />
                {opt}
              </label>
            ))}
          </div>
        </>
      )}

      {/* Navigation */}
      <div className="button-row">
        <button className="btn back" onClick={handleBack}>
          ‹ Back
        </button>
        <button className="btn next" onClick={handleNext} disabled={!canNext}>
          {section < 4 ? 'Next ›' : 'Proceed ›'}
        </button>
      </div>
    </div>
  );
};

export default Survey;
