

-- Daily OHLCV from Polygon
CREATE TABLE market_bar_daily (
  id BIGSERIAL PRIMARY KEY,
  ticker TEXT NOT NULL,
  "date" DATE NOT NULL,
  open NUMERIC(18,6) NOT NULL,
  high NUMERIC(18,6) NOT NULL,
  low  NUMERIC(18,6) NOT NULL,
  close NUMERIC(18,6) NOT NULL,
  volume BIGINT NOT NULL,
  vwap NUMERIC(18,6),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_bar UNIQUE (ticker, "date")
);

-- Strategy streak and last signal per driver ticker (DIA drives both legs)
CREATE TABLE strategy_state (
  id BIGSERIAL PRIMARY KEY,
  driver_ticker TEXT NOT NULL,         -- e.g., 'DIA'
  neg_streak INT NOT NULL DEFAULT 0,   -- consecutive down days for driver
  pos_streak INT NOT NULL DEFAULT 0,   -- consecutive up days for driver
  last_signal TEXT,                    -- 'HOLD' | 'BUY_DIA' | 'SELL_DIA' | 'BUY_SDOW' | 'SELL_SDOW'
  last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_driver UNIQUE (driver_ticker)
);

-- Orders we submit (requested) so we keep an audit trail
CREATE TABLE trade_order (
  id BIGSERIAL PRIMARY KEY,
  ts TIMESTAMPTZ NOT NULL DEFAULT now(),
  action TEXT NOT NULL,                -- 'BUY' | 'SELL'
  ticker TEXT NOT NULL,                -- 'DIA' or 'SDOW'
  qty NUMERIC(18,6) NOT NULL,          -- shares (or fractional if broker supports)
  notional NUMERIC(18,2),              -- optional
  status TEXT NOT NULL DEFAULT 'REQUESTED', -- REQUESTED|ACCEPTED|FILLED|REJECTED
  broker_order_id TEXT,
  broker_msg TEXT
);

-- Position snapshot we maintain locally (optional but useful)
CREATE TABLE position_snapshot (
  id BIGSERIAL PRIMARY KEY,
  ts TIMESTAMPTZ NOT NULL DEFAULT now(),
  ticker TEXT NOT NULL,
  qty NUMERIC(18,6) NOT NULL,
  avg_price NUMERIC(18,6)
);
